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
package co.videofirst.vft.capture.service.impl;

import co.videofirst.vft.capture.configuration.properties.UploadConfig;
import co.videofirst.vft.capture.dao.VideoDao;
import co.videofirst.vft.capture.enums.UploadState;
import co.videofirst.vft.capture.exception.InvalidStateException;
import co.videofirst.vft.capture.exception.VideoUploadException;
import co.videofirst.vft.capture.http.ProgressEntityWrapper;
import co.videofirst.vft.capture.http.ProgressListener;
import co.videofirst.vft.capture.model.video.Upload;
import co.videofirst.vft.capture.model.video.UploadStatus;
import co.videofirst.vft.capture.model.video.Video;
import co.videofirst.vft.capture.service.InfoService;
import co.videofirst.vft.capture.service.UploadService;
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

    private final VideoDao videoDao;

    // Local fields / stateful objects

    @Getter
    @Setter
    private UploadConfig uploadConfig;

    private final BlockingQueue<Video> queue = new LinkedBlockingQueue<>();
    private final Map<String, Video> uploads = new ConcurrentHashMap<>();

    @Autowired
    public DefaultUploadService(InfoService infoService, VideoDao videoDao) {
        uploadConfig = infoService.getInfo().getInfo().getUpload();
        this.videoDao = videoDao;

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
    public void upload(String videoId) {
        if (!uploadConfig.isEnable()) {
            throw new VideoUploadException(
                "Please enable upload configuration (i.e. set `config.system.upload.enable` property to `true`)");
        }

        Video video = getVideo(videoId);

        // Check video is finished i.e. the `finished` timestamp is set.
        if (video.getFinished() == null) {
            throw new InvalidStateException(
                "You can only upload a video which is finished.  Please try again later.");
        }

        // Mark video that it's scheduled for upload
        Upload upload = Upload.schedule(uploadConfig.getUrl());
        video.setUpload(upload);
        videoDao.save(video);

        uploads.put(videoId, video);
        queue.add(video);
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
        // video's off the queue.
        while (true) {
            try {
                // Take off queue and update that upload
                Video video = queue.take();
                uploadToServer(video);
            } catch (InterruptedException e) {
                log.warn("Interruption error", e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${vft_config.upload.purgeFinishedUploadSchedule:2000}")
    public void purgeFinishedUploads() {
        for (String videoId : uploads.keySet()) {
            Video video = uploads.get(videoId);
            LocalDateTime time = LocalDateTime.now()
                .minusSeconds(uploadConfig.getKeepFinishedUploadsInSecs());
            if (video.getUpload() != null &&
                video.getUpload().getState() == UploadState.finished &&
                video.getUpload().getFinished().isBefore(time)) {

                log.info("Removing video " + videoId);
                uploads.remove(videoId);
            }
            log.info("Not removing video " + videoId);
        }
    }

    // Private methods

    /**
     * Retrieve video and throw an exception of the video doesn't exist.
     */
    private Video getVideo(String videoId) {
        Video video = videoDao.findById(videoId);
        if (video == null) {
            throw new InvalidStateException(
                "Video [ " + videoId + " ] doesn't exist! Please re-check video ID.");
        }
        return video;
    }

    /**
     * Upload video upload.
     */
    private void updateVideoUpload(final Video video, final Upload upload) {
        video.setUpload(upload);
        videoDao.save(video);
    }

    /**
     * Upload video to server.
     */
    private void uploadToServer(final Video video) {

        updateVideoUpload(video, video.getUpload().start());
        HttpPost httpPost = getHttpPost(video);

        try {
            // Execute HTTP call
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(httpPost);
            String httpBody = getHttpBody(response);
            int statusCode = response.getStatusLine().getStatusCode();

            // Check status and update accordingly
            if (statusCode == 200) {
                log.trace("Upload successful, body [ " + httpBody + " ]");
                updateVideoUpload(video, video.getUpload().finish());
            } else {
                log.trace("Upload unsuccessful, body [ " + httpBody + " ] / status code [ "
                    + statusCode);
                updateVideoUpload(video, video.getUpload().error(httpBody, statusCode));
            }
        } catch (IOException ioEx) {
            log.warn("IO exception calling upload", ioEx);
            String errorMessage = ioEx.getMessage();
            updateVideoUpload(video, video.getUpload().error(errorMessage, null));
        }
    }

    /**
     * Prepare HTTP Post upload call
     */
    private HttpPost getHttpPost(Video video) {
        // Get video file and data file
        File videoFile = validateExists(video.getVideoFile());
        File dataFile = validateExists(video.getDataFile());

        HttpPost httpPost = new HttpPost(uploadConfig.getUrl());
        uploadConfig.getHeaders().entrySet().stream()
            .forEach(e -> httpPost.setHeader(e.getKey(), e.getValue()));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder
            .addBinaryBody(PARAM_VIDEO, videoFile, ContentType.DEFAULT_BINARY, videoFile.getName());
        builder.addBinaryBody(PARAM_DATA, dataFile, ContentType.DEFAULT_BINARY, dataFile.getName());
        HttpEntity multipart = builder.build();

        ProgressListener pListener = new VideoUploadProgressListener(videoDao, video,
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
     * Video progress listener.
     */
    private static class VideoUploadProgressListener implements ProgressListener {

        // Injected fields

        private final VideoDao videoDao;
        private final Video video;
        private final long updateIntervalMillis;

        // Other fields

        private long curTimeMillis;

        public VideoUploadProgressListener(VideoDao videoDao, Video video,
            long updateIntervalMillis) {
            this.videoDao = videoDao;
            this.video = video;
            this.updateIntervalMillis = updateIntervalMillis;

            curTimeMillis = System.currentTimeMillis();
        }

        @Override
        public void progress(long transferred, long totalBytes) {
            // Only update after current time is greater than an interval of e.g. every 2 seconds.
            if ((System.currentTimeMillis() - curTimeMillis) > updateIntervalMillis) {
                Upload upload = video.getUpload().updateProgress(transferred, totalBytes);
                video.setUpload(upload);
                videoDao.save(video);

                curTimeMillis = System.currentTimeMillis(); // reset time

                System.out.println("Progress - " + transferred + " / " + totalBytes);
            }
        }

    }

}
