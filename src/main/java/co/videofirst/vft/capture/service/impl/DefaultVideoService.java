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

import co.videofirst.vft.capture.dao.VideoDao;
import co.videofirst.vft.capture.enums.VideoState;
import co.videofirst.vft.capture.exception.InvalidParameterException;
import co.videofirst.vft.capture.exception.InvalidStateException;
import co.videofirst.vft.capture.model.display.DisplayUpdate;
import co.videofirst.vft.capture.model.params.VideoFinishParams;
import co.videofirst.vft.capture.model.params.VideoStartParams;
import co.videofirst.vft.capture.model.video.Video;
import co.videofirst.vft.capture.model.video.VideoStatus;
import co.videofirst.vft.capture.model.video.VideoSummary;
import co.videofirst.vft.capture.recorder.VideoRecord;
import co.videofirst.vft.capture.recorder.VideoRecorder;
import co.videofirst.vft.capture.service.DisplayService;
import co.videofirst.vft.capture.service.InfoService;
import co.videofirst.vft.capture.service.VideoService;
import co.videofirst.vft.capture.utils.ConfigUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the high level VideoService interface.
 *
 * @author Bob Marks
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultVideoService implements VideoService {

    // Injected fields

    private final VideoRecorder videoRecorder;
    private final DisplayService displayService;
    private final InfoService infoService;
    private final VideoDao videoDao;

    // Local fields

    private VideoStatus videoStatus = VideoStatus.IDLE;  // only stateful object

    @Override
    public Video select(String videoId) {
        Video video = videoDao.findById(videoId);
        return video;
    }

    @Override
    public List<VideoSummary> list() {
        List<VideoSummary> list = videoDao.list();
        return list;
    }

    @Override
    public VideoStatus status() {
        return videoStatus;
    }

    @Override
    public VideoStatus start(VideoStartParams videoStartParams) {
        validateStart(videoStartParams);

        videoStatus = VideoStatus.start(infoService.getInfo(), videoStartParams);

        if (videoStartParams.record()) {
            record();
        }

        refreshDisplay();
        return status();
    }

    @Override
    public VideoStatus record() {
        if (videoStatus.getState() != VideoState.started) {
            throw new InvalidStateException("Current state is '" + videoStatus.getState() + "' - " +
                "video can only be recoded when state is  'started'.");
        }

        DisplayUpdate displayUpdate = getDisplayUpdate();

        videoStatus = videoStatus.record(displayUpdate.getCapture());
        videoRecorder.record(getVideoRecord());

        refreshDisplay();

        return status();
    }

    @Override
    public VideoStatus stop() {
        if (videoStatus.getState() != VideoState.recording) {
            throw new InvalidParameterException(
                "Current state is '" + videoStatus.getState() + "' - " +
                    "You can only stop a video when it is in a state is 'recording'.");
        }

        videoStatus = videoStatus.stop();
        videoRecorder.stop();
        refreshDisplay();

        return status();
    }

    @Override
    public VideoStatus finish(VideoFinishParams finishVideo) {

        validateFinish(finishVideo);

        if (videoStatus.getState() == VideoState.recording) {
            stop();
        }

        VideoStatus finishedVideoStatus = videoStatus.finish(finishVideo);
        videoDao.save(finishedVideoStatus.getVideo());

        videoStatus = VideoStatus.IDLE; // mark current status as stopped ..
        refreshDisplay();

        return finishedVideoStatus;  // ... although return the finished status to the user
    }

    @Override
    public VideoStatus cancel() {
        videoRecorder.cancel();  // cancel any recording if applicable
        videoStatus = VideoStatus.IDLE; // re-set status
        refreshDisplay();

        return status();
    }

    @Override
    public void delete(String videoId) {
        videoDao.delete(videoId);
    }

    // Private methods

    private Map<String, String> getCategoryMap(Map<String, String> categoryOverrides) {
        List<String> categories = infoService.getInfo().getInfo().getCategories();
        Map<String, String> categoryDefaults = infoService.getInfo().getDefaults().getCategories();
        Map<String, String> categoryMap = ConfigUtils
            .parseCategoryMap(categories, categoryDefaults, categoryOverrides);
        return categoryMap;
    }

    /**
     * Validate start parameters.
     */
    private void validateStart(VideoStartParams videoStartParams) {

        if (videoStatus.getState() != VideoState.idle
            && videoStatus.getState() != VideoState.started) {
            throw new InvalidStateException("Current state is '" + videoStatus.getState() + "' - " +
                "video can only be started when state is 'idle', 'uploaded' or re-started when it is in a state is 'started'. "
                + "Please finish recording or cancel.");
        }

        Map<String, String> getCategoryMap = getCategoryMap(videoStartParams.getCategories());

        String categories = getCategoryMap.entrySet().stream()
            .filter(category -> category.getValue() == null || category.getValue().isEmpty())
            .map(category -> category.getKey())
            .collect(Collectors.joining(", "));
        if (!categories.isEmpty()) {
            throw new InvalidParameterException(
                "Please fill in missing categories [ " + categories + " ]");
        }

        if (videoStartParams.getFeature() == null || videoStartParams.getFeature().isEmpty()) {
            throw new InvalidParameterException("Please fill in missing 'feature' attribute");
        }
        if (videoStartParams.getScenario() == null || videoStartParams.getScenario().isEmpty()) {
            throw new InvalidParameterException("Please fill in missing 'scenario' attribute");
        }
    }

    private void validateFinish(VideoFinishParams finishVideoParams) {
        // Check state first of all
        if (videoStatus.getState() != VideoState.recording
            && videoStatus.getState() != VideoState.stopped) {
            throw new InvalidStateException("Current state is '" + videoStatus.getState() + "' - " +
                "videos can only be finished which is state is `recording` OR `stopped`.");
        }

        if (finishVideoParams.getStatus() == null) {
            throw new InvalidParameterException("Please fill in missing 'status' attribute");
        }
    }

    /**
     * Generate display update.
     */
    private DisplayUpdate getDisplayUpdate() {
        return DisplayUpdate
            .build(infoService.getInfo(), videoStatus.getVideoStartParams().getDisplay());
    }

    /**
     * Return video record.
     */
    private VideoRecord getVideoRecord() {
        if (videoStatus == null || videoStatus.getVideo() == null) {
            log.warn("Video record is null");
            return null;
        }

        Video video = videoStatus.getVideo();
        return VideoRecord.builder()
            .id(video.getId())
            .folder(video.getFolder())
            .format(video.getFormat())
            .capture(video.getCapture())
            .build();
    }

    /**
     * Refresh display.
     */
    private void refreshDisplay() {
        DisplayUpdate displayUpdate = getDisplayUpdate();
        displayService.update(displayUpdate, videoStatus);
    }

}
