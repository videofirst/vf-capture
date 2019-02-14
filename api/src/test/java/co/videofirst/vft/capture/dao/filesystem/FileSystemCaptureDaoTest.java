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

import static co.videofirst.vft.capture.enums.LogTier.L1;
import static co.videofirst.vft.capture.enums.LogTier.L2;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import co.videofirst.vft.capture.enums.CaptureType;
import co.videofirst.vft.capture.enums.TestStatus;
import co.videofirst.vft.capture.model.TestLog;
import co.videofirst.vft.capture.model.capture.Capture;
import co.videofirst.vft.capture.model.capture.CaptureSummary;
import co.videofirst.vft.capture.model.display.DisplayCapture;
import co.videofirst.vft.capture.test.VftTesting;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Unit test to test the methods of FileSystemCaptureDao.
 *
 * @author Bob Marks
 */
public class FileSystemCaptureDaoTest {

    // Constants

    private final LocalDateTime ts1 = LocalDateTime.of(2015, 1, 2, 12, 13, 14);
    private final LocalDateTime ts2 = LocalDateTime.of(2016, 2, 3, 16, 17, 18);
    private final LocalDateTime ts3 = LocalDateTime.of(2017, 3, 4, 19, 20, 21);
    private final LocalDateTime ts4 = LocalDateTime.of(2018, 4, 5, 20, 21, 22);

    // Fields

    private FileSystemCaptureDao target;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws IOException {
        VftTesting.initTestFolders(); // clean out any videos generated

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS);

        target = new FileSystemCaptureDao(objectMapper, VftTesting.VFT_VIDEO_FOLDER);
    }

    @After
    public void tearDown() throws IOException {
        VftTesting.cleanTestFolders(); // delete files again
    }

    @Test
    public void shouldSaveVideo() throws IOException, JSONException {

        Capture capture = Capture.builder()
            .started(ts1)
            .finished(ts2)
            .project("Google Search")
            .feature("Login")
            .scenario("Search by Country")
            .folder("google-search/login/search-by-country/2018-01-30_17-33-47_a9kea")
            .id("2018-01-30_17-33-47_a9kea")
            .sid(999L)
            .type(CaptureType.automated)
            .format("avi")
            .capture(DisplayCapture.builder().x(1).y(2).width(3).height(4).build())
            .environment(ImmutableMap.of("os.arch", "amd64", "os.name", "Windows 10"))
            .meta(ImmutableMap.of("version", "1.2.3-beta"))
            .description("Awesome test")
            .testStatus(TestStatus.error)
            .testError("Awesome error")
            .testLogs(asList(
                TestLog.builder().cat("browser").tier(L1).ts(ts3).log("awesome log 1").build(),
                TestLog.builder().cat("server").tier(L2).ts(ts4).log("awesome log 2").build()))

            .build();

        target.save(capture);

        // Assert that capture meta-data was saved
        assertFolderExists(VftTesting.VFT_VIDEO_FOLDER, "google-search", "login",
            "search-by-country",
            "2018-01-30_17-33-47_a9kea");
        File file = new File(VftTesting.VFT_VIDEO_FOLDER,
            "google-search/login/search-by-country/2018-01-30_17-33-47_a9kea/2018-01-30_17-33-47_a9kea.json");
        assertThat(file).exists();
        String json = new String(Files.readAllBytes(file.toPath()));
        String expectedJson = "{" +
            "    'started': '2015-01-02T12:13:14'," +
            "    'finished': '2016-02-03T16:17:18'," +
            "    'project': 'Google Search'," +
            "    'feature': 'Login'," +
            "    'scenario': 'Search by Country'," +
            "    'folder': 'google-search/login/search-by-country/2018-01-30_17-33-47_a9kea'," +
            "    'id': '2018-01-30_17-33-47_a9kea'," +
            "    'type': 'automated'," +
            "    'sid': 999," +
            "    'format': 'avi'," +
            "    'capture': { 'x': 1, 'y': 2, 'width': 3, 'height': 4 }, " +
            // optional
            "    'meta': { 'version': '1.2.3-beta' }," +
            "    'description': 'Awesome test'," +
            "    'environment': {" +
            "        'os.arch': 'amd64'," +
            "        'os.name': 'Windows 10'" +
            "    }," +
            "    'testStatus': 'error'," +
            "    'testError': 'Awesome error'," +
            "    'testLogs': [{" +
            "        'ts': '2017-03-04T19:20:21'," +
            "        'cat': 'browser'," +
            "        'tier': 'L1'," +
            "        'log': 'awesome log 1'" +
            "    }, {" +
            "        'ts': '2018-04-05T20:21:22'," +
            "        'cat': 'server'," +
            "        'tier': 'L2'," +
            "        'log': 'awesome log 2'" +
            "    }]" +
            "}";
        JSONAssert.assertEquals(expectedJson, json, true);
    }

    @Test
    public void shouldFindById() {

        String id = "2018-02-15_12-14-02_n3jwzb";

        Capture capture = target.findById(id);

        assertThat(capture.getId()).isEqualTo(id);
        assertThat(capture.getProject()).isEqualTo("Moon Rocket");
        assertThat(capture.getFeature()).isEqualTo("Bob Feature");
        assertThat(capture.getScenario()).isEqualTo("Dave Scenario");
        assertThat(capture.getStarted()).isEqualTo(LocalDateTime.of(2018, 2, 15, 12, 14, 02));
        assertThat(capture.getFinished()).isEqualTo(LocalDateTime.of(2018, 2, 15, 12, 14, 03));
        assertThat(capture.getFolder())
            .isEqualTo("moon-rocket/bob-feature/dave-scenario/2018-02-15_12-14-02_n3jwzb");
        assertThat(capture.getFormat()).isEqualTo("avi");
        assertThat(capture.getCapture()).isEqualTo(DisplayCapture.builder()
            .x(0).y(0).width(1920).height(1200).build());
        assertThat(capture.getMeta()).isEmpty();
        assertThat(capture.getType()).isEqualTo(CaptureType.automated);
        assertThat(capture.getSid()).isEqualTo(1234L);
        assertThat(capture.getEnvironment())
            .containsExactly(entry("java.awt.graphicsenv", "sun.awt.Win32GraphicsEnvironment"));
        assertThat(capture.getTestStatus()).isEqualTo(TestStatus.fail);
    }

    @Test
    public void shouldList() {

        String id = "2018-02-15_12-14-02_n3jwzb";

        List<CaptureSummary> videos = target.list();

        assertThat(videos).hasSize(2);
        CaptureSummary video = videos.get(1);  // Detailed assertions
        assertThat(video.getId()).isEqualTo(id);
        assertThat(video.getProject()).isEqualTo("Moon Rocket");
        assertThat(video.getFeature()).isEqualTo("Bob Feature");
        assertThat(video.getScenario()).isEqualTo("Dave Scenario");
        assertThat(video.getStarted()).isEqualTo(LocalDateTime.of(2018, 2, 15, 12, 14, 02));
        assertThat(video.getFinished()).isEqualTo(LocalDateTime.of(2018, 2, 15, 12, 14, 03));
        assertThat(video.getFormat()).isEqualTo("avi");
        assertThat(video.getTestStatus()).isEqualTo(TestStatus.fail);
    }

    @Test
    public void shouldDelete() {

        String id = "2018-02-15_12-14-02_n3jwzb";
        // Check video exists
        File folder = new File(VftTesting.VFT_VIDEO_FOLDER,
            "moon-rocket/bob-feature/dave-scenario/2018-02-15_12-14-02_n3jwzb");
        File dataFile = new File(folder, id + ".json");
        File videoFile = new File(folder, id + ".avi");
        assertThat(dataFile).exists();
        assertThat(videoFile).exists();

        target.delete(id);

        assertThat(dataFile).doesNotExist();
        assertThat(videoFile).doesNotExist();
    }

    // Private methods

    private void assertFolderExists(File baseDir, String... subFolders) {
        File curFile = baseDir;
        for (String subFolder : subFolders) {
            curFile = new File(curFile, subFolder);
            assertThat(curFile).exists().isDirectory();
        }
    }

}
