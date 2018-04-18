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

import co.videofirst.vft.capture.enums.CaptureType;
import co.videofirst.vft.capture.enums.TestStatus;
import co.videofirst.vft.capture.model.TestLog;
import co.videofirst.vft.capture.model.display.DisplayCapture;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Capture object.
 *
 * @author Bob Marks
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "scenarioId", "categories", "feature", "scenario", "type", "description",
    "started", "finished", "folder", "format", "capture", "meta", "environment", "testStatus",
    "testError", "testStackTrace", "testLogs", "upload"})
public class Capture {

    public static final String FORMAT_AVI = "avi"; // only supported format at minute

    private String id;
    private Long scenarioId;
    private Map<String, String> categories;
    private String feature;
    private String scenario;
    private CaptureType type;
    private String description;
    private LocalDateTime started;
    private LocalDateTime finished;
    private String folder;
    private String format;
    private DisplayCapture capture;
    private Map<String, String> meta;
    private Map<String, String> environment;
    private TestStatus testStatus;
    private String testError;
    private String testStackTrace;
    private List<TestLog> testLogs;
    private Upload upload;

    // Don't save these fields
    @JsonIgnore
    private File videoFile;

    @JsonIgnore
    private File dataFile;

}
