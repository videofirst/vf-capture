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
package co.videofirst.vft.capture.dao.filesystem;

import co.videofirst.vft.capture.dao.CaptureDao;
import co.videofirst.vft.capture.exception.VideoOpenException;
import co.videofirst.vft.capture.exception.VideoSaveException;
import co.videofirst.vft.capture.model.capture.Capture;
import co.videofirst.vft.capture.model.capture.CaptureSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * File system implementation of the CaptureDao interface.
 *
 * @author Bob Marks
 */
@Slf4j
@Component
public class FileSystemCaptureDao implements CaptureDao {

    // Constants

    private static final String EXT_JSON = "json";

    // Injected fields

    private final ObjectMapper objectMapper;
    private final File videoFolder;

    //  Private fields

    private final Map<String, File> captureIdCache = new HashMap<>(); // cache

    public FileSystemCaptureDao(ObjectMapper objectMapper,
        @Value("${vft_config.storage.videoFolder}") File videoFolder) {
        this.objectMapper = objectMapper;
        this.videoFolder = videoFolder;

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // FIXME (use property)?
    }

    @Override
    public void save(Capture capture) {
        try {
            File file = getDataFile(capture);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            objectMapper.writeValue(fileOutputStream, capture);
        } catch (IOException e) {
            throw new VideoSaveException("Error saving capture - " + e.getMessage(), e);
        }
    }

    @Override
    public Capture findById(String captureId) {

        // Check cache
        if (captureIdCache.get(captureId) != null) {
            try {
                Capture capture = readVideoFromDataFile(captureIdCache.get(captureId), Capture.class);
                if (capture != null && capture.getId().equals(captureId)) {
                    return capture;
                }
            } catch (VideoOpenException voEx) {
                log.warn("Error opening capture", voEx.getMessage());
            }

            // remove from cache and continue
            captureIdCache.remove(captureId);
        }

        FindFile findFile = new FindFile();
        findVideoFile(captureId, videoFolder, findFile);
        if (findFile.getFile() == null) {
            throw new VideoOpenException("Cannot find a capture for ID - " + captureId);
        }

        Capture capture = readVideoFromDataFile(findFile.getFile(), Capture.class);
        captureIdCache.put(captureId, findFile.getFile());
        return capture;
    }

    @Override
    public List<CaptureSummary> list() {
        // Iterate over file system and read all the files
        List<CaptureSummary> videos = new ArrayList<CaptureSummary>();
        findCaptureSummaries(videoFolder, videos);
        return videos;
    }

    @Override
    public void delete(String captureId) {

        Capture capture = findById(captureId);
        if (capture != null) {
            // delete files, first
            File dir = capture.getDataFile().getParentFile();
            capture.getDataFile().delete();
            capture.getVideoFile().delete();

            // now go up parent by parent until the capture dir ...
            while (!dir.equals(videoFolder)) {
                // .. but if you encounter a folder with other child folders / files then return
                if (dir.list().length != 0) {
                    return;
                }
                dir.delete();
                dir = dir.getParentFile();
            }
        }

    }

    // private methods

    private <V> V readVideoFromDataFile(File file, Class<V> videoType) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            V v = objectMapper.readValue(fileInputStream, videoType);

            // Set additional (non-saved) fields which can be useful e.g. uploading / streaming
            if (v instanceof Capture) {
                Capture capture = (Capture) v;
                capture.setDataFile(getDataFile(capture));
                capture.setVideoFile(getVideoFile(capture));
            }

            return v;
        } catch (IOException e) {
            throw new VideoOpenException("Error open video - " + file.getAbsolutePath(), e);
        }
    }

    private void findVideoFile(String captureId, File curFolder, FindFile findFile) {
        for (File file : curFolder.listFiles()) {
            if (file.isDirectory()) {
                findVideoFile(captureId, file, findFile);
            } else if (file.getName().equalsIgnoreCase(captureId + "." + EXT_JSON)) {
                findFile.setFile(file);
            }
            if (findFile.getFile() != null) {
                return;
            }
        }
    }

    private void findCaptureSummaries(File curFolder, List<CaptureSummary> videos) {
        for (File file : curFolder.listFiles()) {
            if (file.isDirectory()) {
                findCaptureSummaries(file, videos);
            } else if (file.getName().endsWith("." + EXT_JSON)) {
                CaptureSummary captureSummary = readVideoFromDataFile(file, CaptureSummary.class);
                videos.add(captureSummary);
            }
        }
    }

    private File getDataFile(Capture capture) {
        return getFile(capture, EXT_JSON);
    }

    private File getVideoFile(Capture capture) {
        return getFile(capture, capture.getFormat());
    }

    private File getFile(Capture capture, String extension) {
        File fullFolder = new File(videoFolder, capture.getFolder());
        fullFolder.mkdirs();  // make directories if required

        // replace capture extension with ".json" extension
        String filename = capture.getId() + "." + extension;
        File file = new File(fullFolder, filename);
        return file;
    }

    /**
     * Little wrapper class used when we're recursively searching for a file.
     */
    @Data
    static class FindFile {

        private File file;

    }

}
