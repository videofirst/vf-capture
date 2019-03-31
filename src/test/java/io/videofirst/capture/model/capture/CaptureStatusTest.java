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

import static io.videofirst.capture.enums.LogTier.L1;
import static io.videofirst.capture.enums.LogTier.L2;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.videofirst.capture.configuration.properties.CaptureDefaults;
import io.videofirst.capture.enums.TestStatus;
import io.videofirst.capture.model.TestLog;
import io.videofirst.capture.model.display.DisplayCapture;
import io.videofirst.capture.model.info.ConfigInfo;
import io.videofirst.capture.model.info.Info;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Unit test to test the methods of CaptureStatus.
 *
 * @author Bob Marks
 */
public class CaptureStatusTest {

    // Constants

    private static final String DEFAULT_PROJECT = "Google Maps";
    private static final DisplayCapture CAPTURE = DisplayCapture.builder().x(1).y(2).width(3)
        .height(4)
        .build();
    private static final Map<String, String> DEFAULT_ENVIRONMENT =
        ImmutableMap.of("os.arch", "amd64", "os.name", "Windows 10");
    private static final Map<String, String> DEFAULT_META = ImmutableMap
        .of("version", "1.2.3-beta", "author", "David");
    private final LocalDateTime ts1 = LocalDateTime.of(2015, 1, 2, 12, 13, 14);
    private final LocalDateTime ts2 = LocalDateTime.of(2016, 2, 3, 16, 17, 18);

    private static final Info DEFAULT_INFO = Info.builder()
        .info(ConfigInfo.builder()
            .environment(DEFAULT_ENVIRONMENT)
            .build())
        .defaults(CaptureDefaults.builder().project(DEFAULT_PROJECT).build())
        .build();

    private static final CaptureRecordParams VIDEO_RECORD_PARAMS = CaptureRecordParams.builder()
        .project("Google Search")
        .feature("Advanced Search ")
        .scenario(" Search by Country! ")
        .meta(DEFAULT_META)
        .description("Awesome test")
        .build();

    @Test
    public void shouldIdle() {

        CaptureStatus captureStatus = CaptureStatus.STOPPED;

        Capture capture = captureStatus.getCapture();
        assertThat(captureStatus.isRecording()).isFalse();

        assertThat(captureStatus.getProject()).isNull(); // everything else is null
        assertThat(capture.getFeature()).isNull();
        assertThat(captureStatus.getFeature()).isNull();
        assertThat(capture.getScenario()).isNull();
        assertThat(captureStatus.getScenario()).isNull();
        assertThat(capture.getTestStatus()).isNull();
        assertThat(capture.getStarted()).isNull();
        assertThat(capture.getFinished()).isNull();
        assertThat(capture.getFolder()).isNull();
        assertThat(capture.getId()).isNull();
        Assertions.assertThat(capture.getCapture()).isNull();
        assertThat(capture.getFormat()).isNull();
        assertThat(capture.getMeta()).isNull();
        assertThat(capture.getDescription()).isNull();
        assertThat(capture.getTestError()).isNull();
        assertThat(capture.getTestLogs()).isNull();
        assertThat(capture.getEnvironment()).isNull();
    }

    @Test
    public void shouldRecord() {

        CaptureStatus captureStatus = CaptureStatus.STOPPED
            .record(DEFAULT_INFO, VIDEO_RECORD_PARAMS, CAPTURE);

        Capture capture = captureStatus.getCapture();
        assertThat(captureStatus.isRecording()).isTrue();
        assertThat(capture.getProject()).isEqualTo("Google Search");
        assertThat(captureStatus.getProject()).isEqualTo("Google Search");
        assertThat(capture.getFeature()).isEqualTo("Advanced Search");
        assertThat(captureStatus.getFeature()).isEqualTo("Advanced Search");
        assertThat(capture.getScenario()).isEqualTo("Search by Country!");
        assertThat(captureStatus.getScenario()).isEqualTo("Search by Country!");
        assertThat(capture.getTestStatus()).isNull();
        assertThat(capture.getStarted()).isNotNull();
        assertThat(capture.getFinished()).isNull();
        assertThat(capture.getFolder()).matches(
            "google-search/advanced-search/search-by-country/\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        assertThat(capture.getId())
            .matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        Assertions.assertThat(capture.getCapture()).isEqualTo(CAPTURE);
        assertThat(capture.getFormat())
            .isEqualTo(Capture.FORMAT_AVI); // all that is supported at the minute

        assertThat(capture.getMeta()).isEqualTo(DEFAULT_META);
        assertThat(capture.getDescription()).isEqualTo("Awesome test");
        assertThat(capture.getTestError()).isNull();
        assertThat(capture.getTestLogs()).isNull();
        assertThat(capture.getEnvironment()).isEqualTo(DEFAULT_ENVIRONMENT);
    }

    @Test
    public void shouldStop() {

        List<TestLog> logs = asList(
            TestLog.builder().cat("browser").tier(L1).ts(ts1).log("awesome log 1").build(),
            TestLog.builder().cat("server").tier(L2).ts(ts2).log("awesome log 2").build());
        CaptureStopParams captureStopParams = CaptureStopParams.builder()
            .testStatus(TestStatus.fail)
            .meta(ImmutableMap.of("author", "Bob"))
            .description(" even more awesome description ")
            .error(" awesome error ")
            .logs(logs)
            .build();

        CaptureStatus captureStatus = CaptureStatus.STOPPED
            .record(DEFAULT_INFO, VIDEO_RECORD_PARAMS, CAPTURE)
            .stop(captureStopParams);

        Capture capture = captureStatus.getCapture();
        assertThat(captureStatus.isRecording()).isFalse();
        assertThat(capture.getProject()).isEqualTo("Google Search");
        assertThat(captureStatus.getProject()).isEqualTo("Google Search");
        assertThat(capture.getFeature()).isEqualTo("Advanced Search");
        assertThat(captureStatus.getFeature()).isEqualTo("Advanced Search");
        assertThat(capture.getScenario()).isEqualTo("Search by Country!");
        assertThat(captureStatus.getScenario()).isEqualTo("Search by Country!");
        assertThat(capture.getDescription()).isEqualTo("even more awesome description");
        assertThat(capture.getStarted()).isNotNull();
        assertThat(capture.getFinished()).isNotNull();
        assertThat(capture.getFolder()).matches(
            "google-search/advanced-search/search-by-country/\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        assertThat(capture.getId())
            .matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        Assertions.assertThat(capture.getCapture()).isEqualTo(CAPTURE);
        assertThat(capture.getFormat())
            .isEqualTo(Capture.FORMAT_AVI); // all that is supported at the minute
        assertThat(capture.getMeta()).isEqualTo(ImmutableMap
            .of("version", "1.2.3-beta", "author", "Bob"));
        assertThat(capture.getEnvironment()).isEqualTo(DEFAULT_ENVIRONMENT);
        assertThat(capture.getTestStatus()).isEqualTo(TestStatus.fail);
        assertThat(capture.getTestError()).isEqualTo("awesome error");
        assertThat(capture.getTestLogs()).isEqualTo(logs);
    }

}
