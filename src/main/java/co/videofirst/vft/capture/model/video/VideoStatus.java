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
package co.videofirst.vft.capture.model.video;

import static co.videofirst.vft.capture.model.video.Video.FORMAT_AVI;

import co.videofirst.vft.capture.enums.TestPassStatus;
import co.videofirst.vft.capture.enums.VideoState;
import co.videofirst.vft.capture.exception.VideoStatusException;
import co.videofirst.vft.capture.model.display.DisplayCapture;
import co.videofirst.vft.capture.model.info.Info;
import co.videofirst.vft.capture.model.params.VideoFinishParams;
import co.videofirst.vft.capture.model.params.VideoStartParams;
import co.videofirst.vft.capture.model.test.TestLog;
import co.videofirst.vft.capture.utils.ConfigUtils;
import co.videofirst.vft.capture.utils.VftUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * Immutable video status model object.
 *
 * @author Bob Marks
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"state", "categories", "feature", "scenario", "started", "finished",
    "durationSeconds", "folder", "id", "capture", "format", "meta", "description", "environment",
    "testStatus", "testError", "testLogs"})
public class VideoStatus {

    // Constants

    public static VideoStatus IDLE = new VideoStatus();

    @Getter
    private final VideoState state; // Show state attribute ...

    @Getter
    @JsonIgnore
    private final VideoStartParams videoStartParams; // ... but not this one ...

    @Getter
    @JsonIgnore
    private final Video video; // ... or this one ...

    /**
     * Private no-args constructor.
     */
    private VideoStatus() {
        this(new VideoStartParams(), new Video(), VideoState.idle);
    }

    /**
     * This constructor is also private. All access to these are via a static builder method.
     */
    private VideoStatus(VideoStartParams videoStartParams, Video video, VideoState state) {
        this.videoStartParams = videoStartParams;
        this.video = video;
        this.state = state;
    }

    // Static methods

    /**
     * Start a new VideoStatus object which requires start parameters to get it kicked off.
     */
    public static VideoStatus start(Info info, VideoStartParams videoStartParams) {

        // 1) Validation
        validateInfo(info);
        if (videoStartParams == null) {
            throw new VideoStatusException("Please supply a valid video start parameter");
        }

        // 2) Create definitive category map
        Map<String, String> categoryMap = getCategoryMap(info, videoStartParams.getCategories());

        // 3) Create and return video object
        Video video = Video.builder()
            .categories(categoryMap)
            .feature(VftUtils.nullTrim(videoStartParams.getFeature()))
            .scenario(VftUtils.nullTrim(videoStartParams.getScenario()))
            // optional
            .meta(videoStartParams.getMeta())
            .description(VftUtils.nullTrim(videoStartParams.getDescription()))
            .environment(info.getInfo().getEnvironment())

            .build();

        // 4) Create VideoStatus object and return
        VideoStatus videoStatus = new VideoStatus(videoStartParams, video, VideoState.started);
        return videoStatus;
    }

    /**
     * Generate a VideoStatus object from an existing video status object
     */
    public VideoStatus record(DisplayCapture capture) {
        if (state != VideoState.recording) {
            // 1) Set started to now
            LocalDateTime started = LocalDateTime.now();

            // 2) Create id from started time + random string
            String id = VftUtils.generateId(started);

            // 3) Create folder (from categories)
            List<String> categoryFolders = VftUtils.getFolderFriendlyCategories(
                getCategories(), getFeature(), getScenario(),
                id);
            String folder = String.join("/", categoryFolders);

            // 4) Create and return video object from existing one
            Video video = getVideo().toBuilder()
                .started(started)
                .folder(folder)
                .id(id)
                .capture(capture)
                .format(FORMAT_AVI)
                .build();

            // 5) Create VideoStatus object and return
            VideoStatus videoStatus = new VideoStatus(getVideoStartParams(), video,
                VideoState.recording);
            return videoStatus;
        }

        return this; // just return the old one
    }

    public VideoStatus stop() {
        if (state == VideoState.recording) {

            // 1) Update finish and return
            LocalDateTime finished = LocalDateTime.now();
            Video video = getVideo().toBuilder()
                .finished(finished)
                .build();

            // 2) Create VideoStatus object and return
            VideoStatus videoStatus = new VideoStatus(getVideoStartParams(), video,
                VideoState.stopped);
            return videoStatus;
        }
        return this;
    }

    public VideoStatus finish(VideoFinishParams videoFinishParams) {

        if (videoFinishParams == null) {
            throw new VideoStatusException("Please supply a valid video finish parameter");
        }

        // 1) Check if still recording and finish if that's the case.
        VideoStatus stoppedVideoStatus = state == VideoState.recording ? stop() : this;

        // 2) Create video object

        Video oldVideo = stoppedVideoStatus.getVideo();
        Video video = oldVideo.toBuilder()
            .meta(VftUtils.mergeMaps(oldVideo.getMeta(), videoFinishParams.getMeta()))
            .description(
                videoFinishParams.getDescription() != null && !videoFinishParams.getDescription()
                    .isEmpty() ? videoFinishParams.getDescription().trim()
                    : oldVideo.getDescription())
            .testStatus(videoFinishParams.getStatus())
            .testError(VftUtils.nullTrim(videoFinishParams.getError()))
            .testLogs(videoFinishParams.getLogs())
            .build();

        // 3) Create finished VideoStatus object and return
        VideoStatus videoStatus = new VideoStatus(getVideoStartParams(), video,
            VideoState.finished);
        return videoStatus;
    }

    // Public getters (for JSON)

    public Map<String, String> getCategories() {
        return video.getCategories();
    }

    public String getFeature() {
        return video.getFeature();
    }

    public String getScenario() {
        return video.getScenario();
    }

    public LocalDateTime getStarted() {
        return video.getStarted();
    }

    public LocalDateTime getFinished() {
        return video.getFinished();
    }

    public String getFolder() {
        return video.getFolder();
    }

    public String getId() {
        return video.getId();
    }

    public DisplayCapture getCapture() {
        return video.getCapture();
    }

    public String getFormat() {
        return video.getFormat();
    }

    public Map<String, String> getMeta() {
        return video.getMeta();
    }

    public String getDescription() {
        return video.getDescription();
    }

    public TestPassStatus getTestStatus() {
        return video.getTestStatus();
    }

    public String getTestError() {
        return video.getTestError();
    }

    public List<TestLog> getTestLogs() {
        return video.getTestLogs();
    }

    public Map<String, String> getEnvironment() {
        return video.getEnvironment();
    }

    public Double getDurationSeconds() {
        if (video.getStarted() != null) {
            LocalDateTime end =
                video.getFinished() != null ? video.getFinished() : LocalDateTime.now();
            return (double) video.getStarted().until(end, ChronoUnit.MILLIS) / 1000.0;
        }
        return null;
    }

    // Private static methods

    private static Map<String, String> getCategoryMap(Info info,
        Map<String, String> userCategories) {
        List<String> categories = info.getInfo().getCategories();
        Map<String, String> categoryDefaults = info.getDefaults().getCategories();
        Map<String, String> categoryMap = ConfigUtils
            .parseCategoryMap(categories, categoryDefaults, userCategories);
        return categoryMap;
    }

    private static void validateInfo(Info info) {
        if (info == null || info.getInfo() == null || info.getDefaults() == null) {
            throw new VideoStatusException("Please supply a valid info object");
        }
    }

}
