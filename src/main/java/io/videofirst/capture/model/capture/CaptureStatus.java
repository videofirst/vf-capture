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
import io.videofirst.capture.enums.CaptureState;
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
@JsonPropertyOrder({"state", "project", "feature", "scenario", "started", "finished",
    "durationSeconds", "folder", "id", "capture", "format", "meta", "description", "environment",
    "testStatus", "testError", "testLogs"})
public class CaptureStatus {

    // Constants

    public static CaptureStatus IDLE = new CaptureStatus();

    @Getter
    private final CaptureState state; // Show state attribute ...

    @Getter
    @JsonIgnore
    private final CaptureStartParams captureStartParams; // ... but not this one ...

    @Getter
    @JsonIgnore
    private final Capture capture; // ... or this one ...

    /**
     * Private no-args constructor.
     */
    private CaptureStatus() {
        this(new CaptureStartParams(), new Capture(), CaptureState.idle);
    }

    /**
     * This constructor is also private. All access to these are via a static builder method.
     */
    private CaptureStatus(CaptureStartParams captureStartParams, Capture capture,
        CaptureState state) {
        this.captureStartParams = captureStartParams;
        this.capture = capture;
        this.state = state;
    }

    // Static methods

    /**
     * Start a new CaptureStatus object which requires start parameters to get it kicked off.
     */
    public static CaptureStatus start(Info info, CaptureStartParams captureStartParams) {

        // 1) Validation
        validateInfo(info);
        validateProject(captureStartParams.getProject(), info.getDefaults());
        if (captureStartParams == null) {
            throw new VideoStatusException("Please supply a valid capture start parameter");
        } // is this correct?

        // 3) Create and return capture object
        Capture capture = Capture.builder()
            .project(ConfigUtils
                .parseString(captureStartParams.getProject(), info.getDefaults().getProject()))
            .feature(VfCaptureUtils.nullTrim(captureStartParams.getFeature()))
            .scenario(VfCaptureUtils.nullTrim(captureStartParams.getScenario()))
            .type(captureStartParams.getType() == null ? DEFAULT_CAPTURE_TYPE
                : captureStartParams.getType())
            .sid(captureStartParams.getSid())
            // optional
            .meta(captureStartParams.getMeta())
            .description(VfCaptureUtils.nullTrim(captureStartParams.getDescription()))
            .environment(info.getInfo().getEnvironment())

            .build();

        // 4) Create CaptureStatus object and return
        CaptureStatus captureStatus = new CaptureStatus(captureStartParams, capture,
            CaptureState.started);
        return captureStatus;
    }

    /**
     * Generate a CaptureStatus object from an existing capture status object
     */
    public CaptureStatus record(DisplayCapture displayCapture) {
        if (state != CaptureState.recording) {
            // 1) Set started to now
            LocalDateTime started = LocalDateTime.now();

            // 2) Create id from started time + random string
            String id = VfCaptureUtils.generateId(started);

            // 3) Create folder (check if sid is set)
            boolean useSid = captureStartParams.getSid() != null;
            List<String> folders = useSid ?
                VfCaptureUtils.getFolderFriendlyList(getProject(),
                    String.valueOf(captureStartParams.getSid()), id) :
                VfCaptureUtils.getFolderFriendlyList(getProject(), getFeature(), getScenario(), id);
            String folder = String.join("/", folders);

            // 4) Create and return capture object from existing one
            Capture capture = this.getCapture().toBuilder()
                .started(started)
                .folder(folder)
                .id(id)
                .capture(displayCapture)
                .format(FORMAT_AVI)
                .build();

            // 5) Create VideoStatus object and return
            CaptureStatus captureStatus = new CaptureStatus(this.getCaptureStartParams(), capture,
                CaptureState.recording);
            return captureStatus;
        }

        return this; // just return the old one
    }

    public CaptureStatus stop() {
        if (state == CaptureState.recording) {

            // 1) Update finish and return
            LocalDateTime finished = LocalDateTime.now();
            Capture capture = this.getCapture().toBuilder()
                .finished(finished)
                .build();

            // 2) Create VideoStatus object and return
            CaptureStatus captureStatus = new CaptureStatus(this.getCaptureStartParams(), capture,
                CaptureState.stopped);
            return captureStatus;
        }
        return this;
    }

    public CaptureStatus finish(CaptureFinishParams captureFinishParams) {

        if (captureFinishParams == null) {
            throw new VideoStatusException("Please supply a valid capture finish parameter");
        }

        // 1) Check if still recording and finish if that's the case.
        if (state == CaptureState.recording) {
            stop();
        }

        // 2) Create capture object

        Capture oldCapture = this.getCapture();
        Capture capture = oldCapture.toBuilder()
            .meta(VfCaptureUtils.mergeMaps(oldCapture.getMeta(), captureFinishParams.getMeta()))
            .description(
                captureFinishParams.getDescription() != null && !captureFinishParams
                    .getDescription()
                    .isEmpty() ? captureFinishParams.getDescription().trim()
                    : oldCapture.getDescription())
            .testStatus(captureFinishParams.getTestStatus())
            .testError(VfCaptureUtils.nullTrim(captureFinishParams.getError()))
            .testStackTrace(VfCaptureUtils.nullTrim(captureFinishParams.getStackTrace()))
            .testLogs(captureFinishParams.getLogs())
            .build();

        // 3) Create finished VideoStatus object and return
        CaptureStatus captureStatus = new CaptureStatus(this.getCaptureStartParams(), capture,
            CaptureState.finished);
        return captureStatus;
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
