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
package co.videofirst.vft.capture.model.capture;

import static co.videofirst.vft.capture.model.capture.Capture.FORMAT_AVI;

import co.videofirst.vft.capture.enums.TestPassStatus;
import co.videofirst.vft.capture.enums.CaptureState;
import co.videofirst.vft.capture.exception.VideoStatusException;
import co.videofirst.vft.capture.model.display.DisplayCapture;
import co.videofirst.vft.capture.model.info.Info;
import co.videofirst.vft.capture.model.TestLog;
import co.videofirst.vft.capture.utils.ConfigUtils;
import co.videofirst.vft.capture.utils.VftUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({"state", "categories", "feature", "scenario", "started", "finished",
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
    private CaptureStatus(CaptureStartParams captureStartParams, Capture capture, CaptureState state) {
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
        if (captureStartParams == null) {
            throw new VideoStatusException("Please supply a valid capture start parameter");
        }

        // 2) Create definitive category map
        Map<String, String> categoryMap = getCategoryMap(info, captureStartParams.getCategories());

        // 3) Create and return capture object
        Capture capture = Capture.builder()
            .categories(categoryMap)
            .feature(VftUtils.nullTrim(captureStartParams.getFeature()))
            .scenario(VftUtils.nullTrim(captureStartParams.getScenario()))
            // optional
            .meta(captureStartParams.getMeta())
            .description(VftUtils.nullTrim(captureStartParams.getDescription()))
            .environment(info.getInfo().getEnvironment())

            .build();

        // 4) Create CaptureStatus object and return
        CaptureStatus captureStatus = new CaptureStatus(captureStartParams, capture, CaptureState.started);
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
            String id = VftUtils.generateId(started);

            // 3) Create folder (from categories)
            List<String> categoryFolders = VftUtils.getFolderFriendlyCategories(
                getCategories(), getFeature(), getScenario(),
                id);
            String folder = String.join("/", categoryFolders);

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
        if(state == CaptureState.recording) {
            stop();
        }

        // 2) Create capture object

        Capture oldCapture = this.getCapture();
        Capture capture = oldCapture.toBuilder()
            .meta(VftUtils.mergeMaps(oldCapture.getMeta(), captureFinishParams.getMeta()))
            .description(
                captureFinishParams.getDescription() != null && !captureFinishParams.getDescription()
                    .isEmpty() ? captureFinishParams.getDescription().trim()
                    : oldCapture.getDescription())
            .testStatus(captureFinishParams.getTestStatus())
            .testError(VftUtils.nullTrim(captureFinishParams.getError()))
            .testStackTrace(VftUtils.nullTrim(captureFinishParams.getStackTrace()))
            .testLogs(captureFinishParams.getLogs())
            .build();

        // 3) Create finished VideoStatus object and return
        CaptureStatus captureStatus = new CaptureStatus(this.getCaptureStartParams(), capture,
            CaptureState.finished);
        return captureStatus;
    }

    // Public getters (for JSON)

    public Map<String, String> getCategories() {
        return capture.getCategories();
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

    public TestPassStatus getTestStatus() {
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
