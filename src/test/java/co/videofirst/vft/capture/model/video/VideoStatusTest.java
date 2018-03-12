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

import static co.videofirst.vft.capture.enums.LogTier.L1;
import static co.videofirst.vft.capture.enums.LogTier.L2;
import static co.videofirst.vft.capture.model.video.Video.FORMAT_AVI;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import co.videofirst.vft.capture.configuration.properties.VftDefaults;
import co.videofirst.vft.capture.enums.TestPassStatus;
import co.videofirst.vft.capture.enums.VideoState;
import co.videofirst.vft.capture.model.display.DisplayCapture;
import co.videofirst.vft.capture.model.info.ConfigInfo;
import co.videofirst.vft.capture.model.info.Info;
import co.videofirst.vft.capture.model.params.VideoFinishParams;
import co.videofirst.vft.capture.model.params.VideoStartParams;
import co.videofirst.vft.capture.model.test.TestLog;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Unit test to test the methods of VideoStatus.
 *
 * @author Bob Marks
 */
public class VideoStatusTest {

    // Constants

    private static final List<String> CATEGORIES = asList("organisation", "product", "module");
    private static final Map<String, String> DEFAULT_CATEGORIES = ImmutableMap
        .of("organisation", "Google", "product", "Maps");
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
            .categories(CATEGORIES).build())
        .defaults(VftDefaults.builder().categories(DEFAULT_CATEGORIES).build())
        .build();

    private static final VideoStartParams VIDEO_START_PARAMS = VideoStartParams.builder()
        .categories(ImmutableMap.of("organisation", "Google",
            "product", "Search", "module", "Web App"))
        .feature("Advanced Search ")
        .scenario(" Search by Country! ")
        .meta(DEFAULT_META)
        .description("Awesome test")
        .build();

    @Test
    public void shouldIdle() {

        VideoStatus videoStatus = VideoStatus.IDLE;

        Video video = videoStatus.getVideo();
        assertThat(videoStatus.getState()).isEqualTo(VideoState.idle);

        assertThat(videoStatus.getCategories()).isNull(); // everything else is null
        assertThat(video.getCategories()).isNull();
        assertThat(video.getFeature()).isNull();
        assertThat(videoStatus.getFeature()).isNull();
        assertThat(video.getScenario()).isNull();
        assertThat(videoStatus.getScenario()).isNull();
        assertThat(video.getTestStatus()).isNull();
        assertThat(video.getStarted()).isNull();
        assertThat(video.getFinished()).isNull();
        assertThat(video.getFolder()).isNull();
        assertThat(video.getId()).isNull();
        assertThat(video.getCapture()).isNull();
        assertThat(video.getFormat()).isNull();
        assertThat(video.getMeta()).isNull();
        assertThat(video.getDescription()).isNull();
        assertThat(video.getTestError()).isNull();
        assertThat(video.getTestLogs()).isNull();
        assertThat(video.getEnvironment()).isNull();

    }

    @Test
    public void shouldStart() {

        VideoStatus videoStatus = VideoStatus.start(DEFAULT_INFO, VIDEO_START_PARAMS);

        Video video = videoStatus.getVideo();
        assertThat(videoStatus.getState()).isEqualTo(VideoState.started);
        assertThat(videoStatus.getCategories())
            .isEqualTo(ImmutableMap
                .of("organisation", "Google", "product", "Search", "module", "Web App"));
        assertThat(video.getCategories())
            .isEqualTo(ImmutableMap
                .of("organisation", "Google", "product", "Search", "module", "Web App"));
        assertThat(video.getFeature()).isEqualTo("Advanced Search");
        assertThat(videoStatus.getFeature()).isEqualTo("Advanced Search");
        assertThat(video.getScenario()).isEqualTo("Search by Country!");
        assertThat(videoStatus.getScenario()).isEqualTo("Search by Country!");
        assertThat(video.getTestStatus()).isNull();
        assertThat(video.getStarted()).isNull();
        assertThat(video.getFinished()).isNull();
        assertThat(video.getFolder()).isNull();
        assertThat(video.getId()).isNull();
        assertThat(video.getCapture()).isNull();
        assertThat(video.getFormat()).isNull();

        assertThat(video.getMeta()).isEqualTo(DEFAULT_META);
        assertThat(video.getDescription()).isEqualTo("Awesome test");
        assertThat(video.getTestError()).isNull();
        assertThat(video.getTestLogs()).isNull();
        assertThat(video.getEnvironment()).isEqualTo(DEFAULT_ENVIRONMENT);
    }

    @Test
    public void shouldRecord() {

        VideoStatus videoStatus = VideoStatus.start(DEFAULT_INFO, VIDEO_START_PARAMS)
            .record(CAPTURE);

        Video video = videoStatus.getVideo();
        assertThat(videoStatus.getState()).isEqualTo(VideoState.recording);
        assertThat(videoStatus.getCategories())
            .isEqualTo(ImmutableMap
                .of("organisation", "Google", "product", "Search", "module", "Web App"));
        assertThat(video.getCategories())
            .isEqualTo(ImmutableMap
                .of("organisation", "Google", "product", "Search", "module", "Web App"));
        assertThat(video.getFeature()).isEqualTo("Advanced Search");
        assertThat(videoStatus.getFeature()).isEqualTo("Advanced Search");
        assertThat(video.getScenario()).isEqualTo("Search by Country!");
        assertThat(videoStatus.getScenario()).isEqualTo("Search by Country!");
        assertThat(video.getTestStatus()).isNull();
        assertThat(video.getStarted()).isNotNull();
        assertThat(video.getFinished()).isNull();
        assertThat(video.getFolder()).matches(
            "google/search/web-app/advanced-search/search-by-country/\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        assertThat(video.getId()).matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        assertThat(video.getCapture()).isEqualTo(CAPTURE);
        assertThat(video.getFormat()).isEqualTo(FORMAT_AVI); // all that is supported at the minute

        assertThat(video.getMeta()).isEqualTo(DEFAULT_META);
        assertThat(video.getDescription()).isEqualTo("Awesome test");
        assertThat(video.getTestError()).isNull();
        assertThat(video.getTestLogs()).isNull();
        assertThat(video.getEnvironment()).isEqualTo(DEFAULT_ENVIRONMENT);
    }

    @Test
    public void shouldStop() {

        VideoStatus videoStatus = VideoStatus.start(DEFAULT_INFO, VIDEO_START_PARAMS)
            .record(CAPTURE).stop();

        Video video = videoStatus.getVideo();
        assertThat(videoStatus.getState()).isEqualTo(VideoState.stopped);
        assertThat(videoStatus.getCategories())
            .isEqualTo(ImmutableMap
                .of("organisation", "Google", "product", "Search", "module", "Web App"));
        assertThat(video.getCategories())
            .isEqualTo(ImmutableMap
                .of("organisation", "Google", "product", "Search", "module", "Web App"));
        assertThat(video.getFeature()).isEqualTo("Advanced Search");
        assertThat(videoStatus.getFeature()).isEqualTo("Advanced Search");
        assertThat(video.getScenario()).isEqualTo("Search by Country!");
        assertThat(videoStatus.getScenario()).isEqualTo("Search by Country!");
        assertThat(video.getTestStatus()).isNull();
        assertThat(video.getStarted()).isNotNull();
        assertThat(video.getFinished()).isNotNull();
        assertThat(video.getFolder()).matches(
            "google/search/web-app/advanced-search/search-by-country/\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        assertThat(video.getId()).matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        assertThat(video.getCapture()).isEqualTo(CAPTURE);
        assertThat(video.getFormat()).isEqualTo(FORMAT_AVI); // all that is supported at the minute

        assertThat(video.getMeta()).isEqualTo(DEFAULT_META);
        assertThat(video.getDescription()).isEqualTo("Awesome test");
        assertThat(video.getTestError()).isNull();
        assertThat(video.getTestLogs()).isNull();
        assertThat(video.getEnvironment()).isEqualTo(DEFAULT_ENVIRONMENT);
    }

    @Test
    public void shouldFinish() {

        List<TestLog> logs = asList(
            TestLog.builder().cat("browser").tier(L1).ts(ts1).log("awesome log 1").build(),
            TestLog.builder().cat("server").tier(L2).ts(ts2).log("awesome log 2").build());
        VideoFinishParams videoFinishParams = VideoFinishParams.builder()
            .status(TestPassStatus.fail)
            .meta(ImmutableMap.of("author", "Bob"))
            .description(" even more awesome description ")
            .error(" awesome error ")
            .logs(logs)
            .build();

        VideoStatus videoStatus = VideoStatus.start(DEFAULT_INFO, VIDEO_START_PARAMS)
            .record(CAPTURE).stop().finish(videoFinishParams);

        Video video = videoStatus.getVideo();
        assertThat(videoStatus.getState()).isEqualTo(VideoState.finished);
        assertThat(videoStatus.getCategories())
            .isEqualTo(ImmutableMap
                .of("organisation", "Google", "product", "Search", "module", "Web App"));
        assertThat(video.getCategories())
            .isEqualTo(ImmutableMap
                .of("organisation", "Google", "product", "Search", "module", "Web App"));
        assertThat(video.getFeature()).isEqualTo("Advanced Search");
        assertThat(videoStatus.getFeature()).isEqualTo("Advanced Search");
        assertThat(video.getScenario()).isEqualTo("Search by Country!");
        assertThat(videoStatus.getScenario()).isEqualTo("Search by Country!");
        assertThat(video.getDescription()).isEqualTo("even more awesome description");
        assertThat(video.getStarted()).isNotNull();
        assertThat(video.getFinished()).isNotNull();
        assertThat(video.getFolder()).matches(
            "google/search/web-app/advanced-search/search-by-country/\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        assertThat(video.getId()).matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        assertThat(video.getCapture()).isEqualTo(CAPTURE);
        assertThat(video.getFormat()).isEqualTo(FORMAT_AVI); // all that is supported at the minute
        assertThat(video.getMeta()).isEqualTo(ImmutableMap
            .of("version", "1.2.3-beta", "author", "Bob"));
        assertThat(video.getEnvironment()).isEqualTo(DEFAULT_ENVIRONMENT);
        assertThat(video.getTestStatus()).isEqualTo(TestPassStatus.fail);
        assertThat(video.getTestError()).isEqualTo("awesome error");
        assertThat(video.getTestLogs()).isEqualTo(logs);
    }

}
