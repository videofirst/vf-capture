/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-present, Video First Software
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.videofirst.capture.service.impl;

import io.videofirst.capture.configuration.properties.UploadConfig;
import io.videofirst.capture.dao.CaptureDao;
import io.videofirst.capture.enums.UploadState;
import io.videofirst.capture.exception.InvalidStateException;
import io.videofirst.capture.exception.VideoUploadException;
import io.videofirst.capture.http.ProgressEntityWrapper;
import io.videofirst.capture.http.ProgressListener;
import io.videofirst.capture.model.capture.Capture;
import io.videofirst.capture.model.capture.Upload;
import io.videofirst.capture.model.capture.UploadStatus;
import io.videofirst.capture.service.InfoService;
import io.videofirst.capture.service.UploadService;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the UploadService interface.
 *
 * @author Bob Marks
 */
@Slf4j
@Component
public class DefaultUploadService implements UploadService, Runnable {

    // Constants

    private static final int DAO_UPDATE_INTERVAL_MILLIS = 2000; // Update DAO every 2 seconds
    private static final String PARAM_VIDEO = "video";
    private static final String PARAM_DATA = "data";

    // Injected fields

    private final CaptureDao captureDao;

    // Local fields / stateful objects

    @Getter
    @Setter
    private UploadConfig uploadConfig;

    private final BlockingQueue<Capture> queue = new LinkedBlockingQueue<>();
    private final Map<String, Capture> uploads = new ConcurrentHashMap<>();

    @Autowired
    public DefaultUploadService(InfoService infoService, CaptureDao captureDao) {
        uploadConfig = infoService.getInfo().getInfo().getUpload();
        this.captureDao = captureDao;

        // Create thread of execution depending on the number of configured threads
        if (uploadConfig.isEnable()) {
            log.info("Creating " + uploadConfig.getThreads() + " upload consumer threads");
            for (int i = 0; i < uploadConfig.getThreads(); i++) {
                new Thread(this, "upload-consumer").start();
            }
        } else {
            log.info("Uploading not enabled");
        }
    }

    // Methods from `UploadService`

    @Override
    public void upload(String captureId) {
        if (!uploadConfig.isEnable()) {
            throw new VideoUploadException(
                "Please enable upload configuration (i.e. set `capture_config.upload.enable` property to `true`)");
        }

        Capture capture = getCapture(captureId);

        // Check capture is finished i.e. the `finished` timestamp is set.
        if (capture.getFinished() == null) {
            throw new InvalidStateException(
                "You can only upload a capture which is finished.  Please try again later.");
        }

        // Mark capture that it's scheduled for upload
        Upload upload = Upload.schedule(uploadConfig.getUrl());
        capture.setUpload(upload);
        captureDao.save(capture);

        uploads.put(captureId, capture);
        queue.add(capture);
    }

    @Override
    public List<UploadStatus> status() {
        List<UploadStatus> uploadsList = uploads.values().stream()
            .map(video -> new UploadStatus(video))
            .sorted(Comparator.comparing(UploadStatus::getId))
            .collect(Collectors.toList());
        return uploadsList;
    }

    @Override
    public void cancel() {
        // does a soft clear at the minute i.e. doesn't stop running upload consumer threads
        uploads.clear();
        queue.clear();
    }

    public void setUploadConfig(UploadConfig uploadConfig) {
        this.uploadConfig = uploadConfig;
    }

    // Methods from `Runnable`

    @Override
    public void run() {
        // Very simple consumer which runs in a separate Thread. It loops forever and takes
        // capture's off the queue.
        while (true) {
            try {
                // Take off queue and update that upload
                Capture capture = queue.take();
                uploadToServer(capture);
            } catch (InterruptedException e) {
                log.warn("Interruption error", e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${capture_config.upload.purgeFinishedUploadSchedule:2000}")
    public void purgeFinishedUploads() {
        for (String captureId : uploads.keySet()) {
            Capture capture = uploads.get(captureId);
            LocalDateTime time = LocalDateTime.now()
                .minusSeconds(uploadConfig.getKeepFinishedUploadsInSecs());
            if (capture.getUpload() != null &&
                capture.getUpload().getState() == UploadState.finished &&
                capture.getUpload().getFinished().isBefore(time)) {

                log.info("Removing capture " + captureId);
                uploads.remove(captureId);
            }
            log.debug("Not removing capture " + captureId);
        }
    }

    // Private methods

    /**
     * Retrieve capture and throw an exception of the capture doesn't exist.
     */
    private Capture getCapture(String captureId) {
        Capture capture = captureDao.findById(captureId);
        if (capture == null) {
            throw new InvalidStateException(
                "Capture [ " + captureId + " ] doesn't exist! Please re-check capture ID.");
        }
        return capture;
    }

    /**
     * Update capture upload.
     */
    private void updateCaptureUpload(final Capture capture, final Upload upload) {
        capture.setUpload(upload);
        captureDao.save(capture);
    }

    /**
     * Upload capture to server.
     */
    private void uploadToServer(final Capture capture) {

        updateCaptureUpload(capture, capture.getUpload().start());
        HttpPost httpPost = getHttpPost(capture);

        try {
            // Execute HTTP call
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(httpPost);
            String httpBody = getHttpBody(response);
            int statusCode = response.getStatusLine().getStatusCode();

            // Check status and update accordingly
            if (statusCode == 200) {
                log.trace("Upload successful, body [ " + httpBody + " ]");
                updateCaptureUpload(capture, capture.getUpload().finish());
            } else {
                log.trace("Upload unsuccessful, body [ " + httpBody + " ] / status code [ "
                    + statusCode);
                updateCaptureUpload(capture, capture.getUpload().error(httpBody, statusCode));
            }
        } catch (IOException ioEx) {
            log.warn("IO exception calling upload", ioEx);
            String errorMessage = ioEx.getMessage();
            updateCaptureUpload(capture, capture.getUpload().error(errorMessage, null));
        }
    }

    /**
     * Prepare HTTP Post upload call
     */
    private HttpPost getHttpPost(Capture capture) {
        // Get capture file and data file
        File videoFile = validateExists(capture.getVideoFile());
        File dataFile = validateExists(capture.getDataFile());

        HttpPost httpPost = new HttpPost(uploadConfig.getUrl());
        uploadConfig.getHeaders().entrySet().stream()
            .forEach(e -> httpPost.setHeader(e.getKey(), e.getValue()));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder
            .addBinaryBody(PARAM_VIDEO, videoFile, ContentType.DEFAULT_BINARY, videoFile.getName());
        builder.addBinaryBody(PARAM_DATA, dataFile, ContentType.DEFAULT_BINARY, dataFile.getName());
        HttpEntity multipart = builder.build();

        ProgressListener pListener = new VideoUploadProgressListener(captureDao, capture,
            DAO_UPDATE_INTERVAL_MILLIS);
        httpPost.setEntity(new ProgressEntityWrapper(multipart, pListener));
        return httpPost;
    }

    private String getHttpBody(HttpResponse httpResponse) {
        HttpEntity responseEntity = httpResponse.getEntity();
        if (responseEntity != null) {
            String response = null;
            try {
                response = EntityUtils.toString(responseEntity);
            } catch (IOException e) {
                log.warn("IOException retrieving message from HTTP body", e);
            }
            return response;
        }
        return null;
    }

    private File validateExists(File file) {
        if (file == null || !file.exists()) {
            throw new VideoUploadException(
                "Can't upload - file doesn't exist on disk " + file.getName());
        }
        return file;
    }

    // Private classes

    /**
     * Capture progress listener.
     */
    private static class VideoUploadProgressListener implements ProgressListener {

        // Injected fields

        private final CaptureDao captureDao;
        private final Capture capture;
        private final long updateIntervalMillis;

        // Other fields

        private long curTimeMillis;

        public VideoUploadProgressListener(CaptureDao captureDao, Capture capture,
            long updateIntervalMillis) {
            this.captureDao = captureDao;
            this.capture = capture;
            this.updateIntervalMillis = updateIntervalMillis;

            curTimeMillis = System.currentTimeMillis();
        }

        @Override
        public void progress(long transferred, long totalBytes) {
            // Only update after current time is greater than an interval of e.g. every 2 seconds.
            if ((System.currentTimeMillis() - curTimeMillis) > updateIntervalMillis) {
                Upload upload = capture.getUpload().updateProgress(transferred, totalBytes);
                capture.setUpload(upload);
                captureDao.save(capture);

                curTimeMillis = System.currentTimeMillis(); // reset time

                System.out.println("Progress - " + transferred + " / " + totalBytes);
            }
        }

    }

}
