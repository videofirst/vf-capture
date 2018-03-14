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

import co.videofirst.vft.capture.dao.VideoDao;
import co.videofirst.vft.capture.exception.VideoOpenException;
import co.videofirst.vft.capture.exception.VideoSaveException;
import co.videofirst.vft.capture.model.video.Video;
import co.videofirst.vft.capture.model.video.VideoSummary;
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
 * File system implementation of the VideoDao interface.
 *
 * @author Bob Marks
 */
@Slf4j
@Component
public class FileSystemVideoDao implements VideoDao {

    // Constants

    private static final String EXT_JSON = "json";

    // Injected fields

    private final ObjectMapper objectMapper;
    private final File videoFolder;

    //  Private fields

    private final Map<String, File> videoIdCache = new HashMap<>(); // cache

    public FileSystemVideoDao(ObjectMapper objectMapper,
        @Value("${vft_config.storage.videoFolder}") File videoFolder) {
        this.objectMapper = objectMapper;
        this.videoFolder = videoFolder;

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // FIXME (use property)?
    }

    @Override
    public void save(Video video) {
        try {
            File file = getDataFile(video);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            objectMapper.writeValue(fileOutputStream, video);
        } catch (IOException e) {
            throw new VideoSaveException("Error saving video - " + e.getMessage(), e);
        }
    }

    @Override
    public Video findById(String videoId) {

        // Check cache
        if (videoIdCache.get(videoId) != null) {
            try {
                Video video = readVideoFromDataFile(videoIdCache.get(videoId), Video.class);
                if (video != null && video.getId().equals(videoId)) {
                    return video;
                }
            } catch (VideoOpenException voEx) {
                log.warn("Error opening video", voEx.getMessage());
            }

            // remove from cache and continue
            videoIdCache.remove(videoId);
        }

        FindFile findFile = new FindFile();
        findVideoFile(videoId, videoFolder, findFile);
        if (findFile.getFile() == null) {
            throw new VideoOpenException("Cannot find a video for ID - " + videoId);
        }

        Video video = readVideoFromDataFile(findFile.getFile(), Video.class);
        videoIdCache.put(videoId, findFile.getFile());
        return video;
    }

    @Override
    public List<VideoSummary> list() {
        // Iterate over file system and read all the files
        List<VideoSummary> videos = new ArrayList<VideoSummary>();
        findVideoSummaries(videoFolder, videos);
        return videos;
    }

    @Override
    public void delete(String videoId) {

        Video video = findById(videoId);
        if (video != null) {
            // delete files, first
            File dir = video.getDataFile().getParentFile();
            video.getDataFile().delete();
            video.getVideoFile().delete();

            // now go up parent by parent until the video dir ...
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
            if (v instanceof Video) {
                Video video = (Video) v;
                video.setDataFile(getDataFile(video));
                video.setVideoFile(getVideoFile(video));
            }

            return v;
        } catch (IOException e) {
            throw new VideoOpenException("Error open video - " + file.getAbsolutePath(), e);
        }
    }

    private void findVideoFile(String videoId, File curFolder, FindFile findFile) {
        for (File file : curFolder.listFiles()) {
            if (file.isDirectory()) {
                findVideoFile(videoId, file, findFile);
            } else if (file.getName().equalsIgnoreCase(videoId + "." + EXT_JSON)) {
                findFile.setFile(file);
            }
            if (findFile.getFile() != null) {
                return;
            }
        }
    }

    private void findVideoSummaries(File curFolder, List<VideoSummary> videos) {
        for (File file : curFolder.listFiles()) {
            if (file.isDirectory()) {
                findVideoSummaries(file, videos);
            } else if (file.getName().endsWith("." + EXT_JSON)) {
                VideoSummary videoSummary = readVideoFromDataFile(file, VideoSummary.class);
                videos.add(videoSummary);
            }
        }
    }

    private File getDataFile(Video video) {
        return getFile(video, EXT_JSON);
    }

    private File getVideoFile(Video video) {
        return getFile(video, video.getFormat());
    }

    private File getFile(Video video, String extension) {
        File fullFolder = new File(videoFolder, video.getFolder());
        fullFolder.mkdirs();  // make directories if required

        // replace video extension with ".json" extension
        String filename = video.getId() + "." + extension;
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
