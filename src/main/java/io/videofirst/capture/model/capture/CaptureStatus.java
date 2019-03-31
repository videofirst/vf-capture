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
package io.videofirst.capture.model.capture;

import static io.videofirst.capture.enums.CaptureType.DEFAULT_CAPTURE_TYPE;
import static io.videofirst.capture.model.capture.Capture.FORMAT_AVI;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.videofirst.capture.configuration.properties.CaptureDefaults;
import io.videofirst.capture.enums.CaptureType;
import io.videofirst.capture.enums.TestStatus;
import io.videofirst.capture.exception.InvalidParameterException;
import io.videofirst.capture.exception.VideoStatusException;
import io.videofirst.capture.model.TestLog;
import io.videofirst.capture.model.display.DisplayCapture;
import io.videofirst.capture.model.info.Info;
import io.videofirst.capture.utils.ConfigUtils;
import io.videofirst.capture.utils.VfCaptureUtils;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * Immutable capture status model object.
 *
 * @author Bob Marks
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"isRecording", "project", "feature", "scenario", "started", "finished",
    "durationSeconds", "folder", "id", "capture", "format", "meta", "description", "environment",
    "testStatus", "testError", "testLogs"})
public class CaptureStatus {

    // Constants

    public static CaptureStatus STOPPED = new CaptureStatus();

    @Getter
    private final boolean isRecording; // Show state attribute ...

    @Getter
    @JsonIgnore
    private final CaptureRecordParams captureRecordParams; // ... but not this one ...

    @Getter
    @JsonIgnore
    private final Capture capture; // ... or this one ...

    /**
     * Private no-args constructor.
     */
    private CaptureStatus() {
        this(new CaptureRecordParams(), new Capture(), false);
    }

    /**
     * This constructor is also private. All access to these are via a static builder method.
     */
    private CaptureStatus(CaptureRecordParams captureRecordParams, Capture capture,
        boolean isRecording) {
        this.captureRecordParams = captureRecordParams;
        this.capture = capture;
        this.isRecording = isRecording;
    }

    // Static methods

    /**
     * Generate a CaptureStatus object from an existing capture status object
     */
    public CaptureStatus record(Info info, CaptureRecordParams captureRecordParams,
        DisplayCapture displayCapture) {
        if (!isRecording) {
            // 1) Validation
            validateInfo(info);
            validateProject(captureRecordParams.getProject(), info.getDefaults());

            // 2) Set started to now
            LocalDateTime started = LocalDateTime.now();

            // 3) Create id from started time + random string
            String id = VfCaptureUtils.generateId(started);

            // 4) Create capture object
            Capture capture = Capture.builder()
                .project(ConfigUtils
                    .parseString(captureRecordParams.getProject(), info.getDefaults().getProject()))
                .feature(VfCaptureUtils.nullTrim(captureRecordParams.getFeature()))
                .scenario(VfCaptureUtils.nullTrim(captureRecordParams.getScenario()))
                .type(captureRecordParams.getType() == null ? DEFAULT_CAPTURE_TYPE
                    : captureRecordParams.getType())
                .sid(captureRecordParams.getSid())
                // optional
                .meta(captureRecordParams.getMeta())
                .description(VfCaptureUtils.nullTrim(captureRecordParams.getDescription()))
                .environment(info.getInfo().getEnvironment())
                .started(started)
                .id(id)
                .capture(displayCapture)
                .format(FORMAT_AVI)
                .build();

            // 4) Create folder (check if sid is set)
            boolean useSid = captureRecordParams.getSid() != null;
            List<String> folders = useSid ?
                VfCaptureUtils.getFolderFriendlyList(
                    getProject(),
                    String.valueOf(capture.getSid()),
                    id) :
                VfCaptureUtils.getFolderFriendlyList(
                    capture.getProject(),
                    capture.getFeature(),
                    capture.getScenario(),
                    id);
            capture.setFolder(String.join("/", folders));

            // 5) Create CaptureStatus object and return
            CaptureStatus captureStatus = new CaptureStatus(this.getCaptureRecordParams(), capture,
                true);
            return captureStatus;
        }

        return this; // just return the current one
    }

    public CaptureStatus stop(CaptureStopParams captureStopParams) {
        if (isRecording) {

            // 1) Update finish and return
            LocalDateTime finished = LocalDateTime.now();

            Capture oldCapture = this.getCapture();
            Capture capture = oldCapture.toBuilder()
                .finished(finished)
                .meta(VfCaptureUtils.mergeMaps(oldCapture.getMeta(), captureStopParams.getMeta()))
                .description(
                    captureStopParams.getDescription() != null && !captureStopParams
                        .getDescription()
                        .isEmpty() ? captureStopParams.getDescription().trim()
                        : oldCapture.getDescription())
                .testStatus(captureStopParams.getTestStatus())
                .testError(VfCaptureUtils.nullTrim(captureStopParams.getError()))
                .testStackTrace(VfCaptureUtils.nullTrim(captureStopParams.getStackTrace()))
                .testLogs(captureStopParams.getLogs())
                .build();

            // 2) Create VideoStatus object and return
            CaptureStatus captureStatus = new CaptureStatus(this.getCaptureRecordParams(), capture,
                false);
            return captureStatus;
        }
        return this;
    }

    // Public getters (for JSON)

    public String getProject() {
        return capture.getProject();
    }

    public String getFeature() {
        return capture.getFeature();
    }

    public String getScenario() {
        return capture.getScenario();
    }

    public LocalDateTime getStarted() {
        return capture.getStarted();
    }

    public LocalDateTime getFinished() {
        return capture.getFinished();
    }

    public String getFolder() {
        return capture.getFolder();
    }

    public String getId() {
        return capture.getId();
    }

    public Long getSid() {
        return capture.getSid();
    }

    public CaptureType getType() {
        return capture.getType();
    }

    @JsonProperty("capture")
    public DisplayCapture getDisplayCapture() {
        return capture.getCapture();
    }

    public String getFormat() {
        return capture.getFormat();
    }

    public Map<String, String> getMeta() {
        return capture.getMeta();
    }

    public String getDescription() {
        return capture.getDescription();
    }

    public TestStatus getTestStatus() {
        return capture.getTestStatus();
    }

    public String getTestError() {
        return capture.getTestError();
    }

    public String getTestStackTrace() {
        return capture.getTestStackTrace();
    }

    public List<TestLog> getTestLogs() {
        return capture.getTestLogs();
    }

    public Map<String, String> getEnvironment() {
        return capture.getEnvironment();
    }

    public Double getDurationSeconds() {
        if (capture.getStarted() != null) {
            LocalDateTime end =
                capture.getFinished() != null ? capture.getFinished() : LocalDateTime.now();
            return (double) capture.getStarted().until(end, ChronoUnit.MILLIS) / 1000.0;
        }
        return null;
    }

    // Private static methods

    private static void validateInfo(Info info) {
        if (info == null || info.getInfo() == null || info.getDefaults() == null) {
            throw new VideoStatusException("Please supply a valid info object");
        }
    }

    private static void validateProject(String project, CaptureDefaults defaults) {
        if ((project == null || project.isEmpty()) && (defaults.getProject() == null || defaults
            .getProject().trim().isEmpty())) {
            throw new InvalidParameterException(
                "Please fill in missing project, either in start params OR in the configuration default section");
        }
    }

}
