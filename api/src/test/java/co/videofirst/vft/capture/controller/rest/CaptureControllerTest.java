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
package co.videofirst.vft.capture.controller.rest;

import static co.videofirst.vft.capture.enums.LogTier.L1;
import static co.videofirst.vft.capture.enums.LogTier.L2;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import co.videofirst.vft.capture.enums.TestPassStatus;
import co.videofirst.vft.capture.model.params.CaptureFinishParams;
import co.videofirst.vft.capture.model.params.CaptureStartParams;
import co.videofirst.vft.capture.model.test.TestLog;
import co.videofirst.vft.capture.test.VftTesting;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.revinate.assertj.json.JsonPathAssert;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller test to test the REST methods of CaptureController.
 *
 * @author Bob Marks
 */
public class CaptureControllerTest extends AbstractControllerTest {

    // Constants

    private static final Map<String, String> DEFAULT_META = ImmutableMap
        .of("version", "1.2.3-beta", "author", "David");
    private static final LocalDateTime TS1 = LocalDateTime.of(2015, 1, 2, 12, 13, 14);
    private static final LocalDateTime TS2 = LocalDateTime.of(2016, 2, 3, 16, 17, 18);
    private static final String MOCK_CAPTURE_ID = "2018-02-15_12-14-02_n3jwzb";

    // Capture start params

    private static final CaptureStartParams CAPTURE_START_PARAMS_MIN = CaptureStartParams.builder()
        .feature("Bob Feature").scenario("Dave Scenario").build();
    private static final CaptureStartParams CAPTURE_START_PARAMS_MIN_NO_RECORD = CaptureStartParams
        .builder()
        .feature("Bob Feature").scenario("Dave Scenario").record("false").build();
    private static final CaptureStartParams CAPTURE_START_PARAMS_MAX = CaptureStartParams.builder()
        .categories(ImmutableMap.of("organisation", "Google",
            "product", "Search", "module", "Web App"))
        .feature("Advanced Search ")
        .scenario(" Search by Country! ")
        .meta(DEFAULT_META)
        .description("Awesome test")
        .build();

    // Capture finish params

    private static final CaptureFinishParams CAPTURE_FINISH_PARAMS_MIN = CaptureFinishParams.builder()
        .testStatus(TestPassStatus.fail).build();
    private static final CaptureFinishParams CAPTURE_FINISH_PARAMS_MAX = CaptureFinishParams.builder()
        .testStatus(TestPassStatus.fail)
        .meta(ImmutableMap.of("author", "Bob", "extra", "stuff"))
        .description(" even more awesome description ")
        .error(" awesome error ")
        .logs(asList(
            TestLog.builder().cat("browser").tier(L1).ts(TS1).log("awesome log 1").build(),
            TestLog.builder().cat("server").tier(L2).ts(TS2).log("awesome log 2").build()))
        .build();

    // ===========================================
    // [ /api/captures ] GET
    // ===========================================

    @Test
    public void shouldRetrieveVideos() throws JSONException {

        ResponseEntity<String> response = videos();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "[{" +
            "    'id': '2018-02-15_12-14-02_n3jwzb'," +
            "    'categories': {" +
            "        'organisation': 'Acme'," +
            "        'product': 'Moon Rocket'," +
            "        'module': 'UI'" +
            "    }," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'," +
            "    'started': '2018-02-15T12:14:02'," +
            "    'finished': '2018-02-15T12:14:03'," +
            "    'format': 'avi'," +
            "    'testStatus': 'fail'" +
            "}, {" +
            "    'id': '2018-02-23_10-13-25_9ip93m'," +
            "    'categories': {" +
            "        'organisation': 'Google'," +
            "        'product': 'Search'," +
            "        'module': 'Browser Search'" +
            "    }," +
            "    'feature': 'Home Page Search'," +
            "    'scenario': 'Property Search in Belfast'," +
            "    'started': '2018-02-23T10:13:25.256'," +
            "    'finished': '2018-02-23T10:13:30.184'," +
            "    'format': 'avi'," +
            "    'testStatus': 'pass'" +
            "}]";
        JSONAssert.assertEquals(expectedJson, response.getBody(), true);
    }

    // ===========================================
    // [ /api/captures/<captureId> ] GET
    // ===========================================

    @Test
    public void shouldRetrieveCaptureById() throws JSONException {

        ResponseEntity<String> response = video(MOCK_CAPTURE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "  'id': '2018-02-15_12-14-02_n3jwzb'," +
            "  'categories': {" +
            "    'organisation': 'Acme'," +
            "    'product': 'Moon Rocket'," +
            "    'module': 'UI'" +
            "  }," +
            "  'feature': 'Bob Feature'," +
            "  'scenario': 'Dave Scenario'," +
            "  'started': '2018-02-15T12:14:02'," +
            "  'finished': '2018-02-15T12:14:03'," +
            "  'folder': 'acme/moon-rocket/ui/bob-feature/dave-scenario/2018-02-15_12-14-02_n3jwzb',"
            +
            "  'format': 'avi'," +
            "  'capture': {" +
            "    'x': 0," +
            "    'y': 0," +
            "    'width': 1920," +
            "    'height': 1200" +
            "  }," +
            "  'meta': {}," +
            "  'environment': {" +
            "    'java.awt.graphicsenv': 'sun.awt.Win32GraphicsEnvironment'" +
            "  }," +
            "  'testStatus': 'fail'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), true);
    }

    // ===========================================
    // [ /api/captures/start ] POST
    // ===========================================

    @Test
    public void shouldStartMinParamsWithNoRecord() throws JSONException {

        ResponseEntity<String> response = startVideo(CAPTURE_START_PARAMS_MIN_NO_RECORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'state': 'started'," +
            "    'categories': {" +
            "        'organisation': 'Acme'," +
            "        'product': 'Moon Rocket'," +
            "        'module': 'UI'" +
            "    }," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    @Test
    public void shouldStartMinParamsWithRecord() throws JSONException {

        ResponseEntity<String> response = startVideo(CAPTURE_START_PARAMS_MIN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'state': 'recording'," +
            "    'categories': {" +
            "        'organisation': 'Acme'," +
            "        'product': 'Moon Rocket'," +
            "        'module': 'UI'" +
            "    }," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'," +
            "    'format': 'avi'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsString("$.started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.durationSeconds").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.folder")
            .startsWith("acme/moon-rocket/ui/bob-feature/dave-scenario/");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    @Test
    public void shouldStartWithMaximumParams() throws JSONException {

        ResponseEntity<String> response = startVideo(CAPTURE_START_PARAMS_MAX);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'state': 'recording'," +
            "    'categories': {" +
            "        'organisation': 'Google'," +
            "        'product': 'Search'," +
            "        'module': 'Web App'" +
            "    }," +
            "    'feature': 'Advanced Search'," +
            "    'scenario': 'Search by Country!'," +
            "    'format': 'avi'," +
            "    'meta': {" +
            "        'version': '1.2.3-beta'," +
            "        'author': 'David'" +
            "    }," +
            "    'description': 'Awesome test'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsString("$.started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.durationSeconds").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.folder")
            .startsWith("google/search/web-app/advanced-search/search-by-country/");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    // ===========================================
    // [ /api/captures/record ] POST
    // ===========================================

    @Test
    public void shouldRecord() throws JSONException {

        startVideo(CAPTURE_START_PARAMS_MIN_NO_RECORD); // must specify record, otherwise no need
        ResponseEntity<String> response = recordVideo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'state': 'recording'," +
            "    'categories': {" +
            "        'organisation': 'Acme'," +
            "        'product': 'Moon Rocket'," +
            "        'module': 'UI'" +
            "    }," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'," +
            "    'format': 'avi'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsString("$.started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.durationSeconds").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.folder")
            .startsWith("acme/moon-rocket/ui/bob-feature/dave-scenario/");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    // FIXME - error states

    // ===========================================
    // [ /api/captures/stop ] POST
    // ===========================================

    @Test
    public void shouldStop() throws JSONException {

        startVideo(CAPTURE_START_PARAMS_MIN);
        ResponseEntity<String> response = stopVideo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'state': 'stopped'," +
            "    'categories': {" +
            "        'organisation': 'Acme'," +
            "        'product': 'Moon Rocket'," +
            "        'module': 'UI'" +
            "    }," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'," +
            "    'format': 'avi'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsString("$.started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.finished").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.durationSeconds").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.folder")
            .startsWith("acme/moon-rocket/ui/bob-feature/dave-scenario/");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    // FIXME - error states

    // ===========================================
    // [ /api/captures/finish ] POST
    // ===========================================

    @Test
    public void shouldFinishWithMinParams() throws JSONException {

        startVideo(CAPTURE_START_PARAMS_MIN);
        ResponseEntity<String> response = finishVideo(CAPTURE_FINISH_PARAMS_MIN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'state': 'finished'," +
            "    'categories': {" +
            "        'organisation': 'Acme'," +
            "        'product': 'Moon Rocket'," +
            "        'module': 'UI'" +
            "    }," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'," +
            "    'format': 'avi'," +
            "    'meta': {}" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsString("$.started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.finished").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.durationSeconds").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.folder")
            .startsWith("acme/moon-rocket/ui/bob-feature/dave-scenario/");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    @Test
    public void shouldFinishWithMaxParams() throws JSONException {

        startVideo(CAPTURE_START_PARAMS_MAX);
        ResponseEntity<String> response = finishVideo(CAPTURE_FINISH_PARAMS_MAX);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'state': 'finished'," +
            "    'categories': {" +
            "        'organisation': 'Google'," +
            "        'product': 'Search'," +
            "        'module': 'Web App'" +
            "    }," +
            "    'feature': 'Advanced Search'," +
            "    'scenario': 'Search by Country!'," +
            "    'format': 'avi'," +
            "    'meta': {" +
            "        'version': '1.2.3-beta'," +
            "        'author': 'Bob'," +
            "        'extra': 'stuff'" +
            "    }," +
            "    'description': 'even more awesome description'," +
            "    'testStatus': 'fail'," +
            "    'testError': 'awesome error'," +
            "    'testLogs': [{" +
            "        'ts': '2015-01-02T12:13:14'," +
            "        'cat': 'browser'," +
            "        'tier': 'L1'," +
            "        'log': 'awesome log 1'" +
            "    }, {" +
            "        'ts': '2016-02-03T16:17:18'," +
            "        'cat': 'server'," +
            "        'tier': 'L2'," +
            "        'log': 'awesome log 2'" +
            "    }]" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsString("$.started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.finished").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.durationSeconds").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.folder")
            .startsWith(
                "google/search/web-app/advanced-search/search-by-country/");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    // ===========================================
    // [ /api/captures/status ] GET
    // ===========================================

    @Test
    public void shouldGetsStatusWithStartedUsingMinParamsAndNoRecord() throws JSONException {

        startVideo(CAPTURE_START_PARAMS_MIN_NO_RECORD);
        ResponseEntity<String> response = statusVideo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'state': 'started'," +
            "    'categories': {" +
            "        'organisation': 'Acme'," +
            "        'product': 'Moon Rocket'," +
            "        'module': 'UI'" +
            "    }," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    // ===========================================
    // [ /api/captures/<captureId> ] DELETE
    // ===========================================

    @Test
    public void shouldDeleteVideo() {

        // Check video exists
        File folder = new File(VftTesting.VFT_VIDEO_FOLDER,
            "acme/moon-rocket/ui/bob-feature/dave-scenario/2018-02-15_12-14-02_n3jwzb");
        File dataFile = new File(folder, MOCK_CAPTURE_ID + ".json");
        File videoFile = new File(folder, MOCK_CAPTURE_ID + ".avi");
        assertThat(dataFile).exists();
        assertThat(videoFile).exists();

        assertThat(deleteVideo(MOCK_CAPTURE_ID).getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(dataFile).doesNotExist();
        assertThat(videoFile).doesNotExist();
    }

    // ===========================================
    // [ /api/captures/cancel ]
    // ===========================================

    @Test
    public void shouldCancelFromIdleState() throws JSONException {
        ResponseEntity<String> response = statusVideo();

        cancelAndAssertStatusIsIdle("$.state", response);
    }

    @Test
    public void shouldCancelFromStartedState() throws JSONException {
        ResponseEntity<String> response = startVideo(CAPTURE_START_PARAMS_MIN_NO_RECORD);

        cancelAndAssertStatusIsIdle("$.feature", response);
    }

    @Test
    public void shouldCancelFromRecordingState() throws JSONException {
        ResponseEntity<String> response = startVideo(CAPTURE_START_PARAMS_MIN);

        cancelAndAssertStatusIsIdle("$.started", response);
    }

    @Test
    public void shouldCancelFromStoppedState() throws JSONException {
        startVideo(CAPTURE_START_PARAMS_MIN);
        ResponseEntity<String> response = stopVideo();

        cancelAndAssertStatusIsIdle("$.finished", response);
    }

    @Test
    public void shouldCancelFromFinishedState() throws JSONException {
        startVideo(CAPTURE_START_PARAMS_MIN);
        ResponseEntity<String> response = finishVideo(CAPTURE_FINISH_PARAMS_MIN);

        cancelAndAssertStatusIsIdle("$.testStatus", response);
    }

    // ===========================================
    // [ /api/captures/upload ]
    // ===========================================

    @Test
    public void shouldUpload() throws Exception {

        ResponseEntity<String> response = uploadById("2018-02-15_12-14-02_n3jwzb");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsInteger("$.length()").isEqualTo(1);
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].id")
            .isEqualTo("2018-02-15_12-14-02_n3jwzb");
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].state")
            .isNotNull(); // we have no control over what the state will be be as it's multi-threaded.
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].url")
            .matches("http:\\/\\/localhost:\\d+\\/mock-upload");
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].scheduled").isNotNull();

        verify(mockUploadService, timeout(5000))
            .upload(any(MultipartFile.class), any(MultipartFile.class));

        // Call status until upload finishes
        await().atMost(5, SECONDS)
            .untilAsserted(() -> assertThat(uploadStatusState()).isEqualTo("finished"));
        response = uploadStatus();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsInteger("$.length()").isEqualTo(1);
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].id")
            .isEqualTo("2018-02-15_12-14-02_n3jwzb");
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].state").isEqualTo("finished");
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].url")
            .matches("http:\\/\\/localhost:\\d+\\/mock-upload");
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].scheduled").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].updated").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$[0].finished").isNotNull();
    }

    // Private methods

    /**
     * Call /api//videos GET endpoint.
     */
    private ResponseEntity<String> videos() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate
            .exchange(urlWithPort("/api/captures"), HttpMethod.GET, entity, String.class);
    }

    /**
     * Call /api/captures/[captureId] GET endpoint.
     */
    private ResponseEntity<String> video(String captureId) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/api/captures/" + captureId), HttpMethod.GET, entity, String.class);
    }

    /**
     * Call /api/captures/start POST endpoint.
     */
    private ResponseEntity<String> startVideo(CaptureStartParams captureStartParams) {
        HttpEntity<CaptureStartParams> entity = new HttpEntity<>(captureStartParams, headers);
        return restTemplate.exchange(
            urlWithPort("/api/captures/start"), HttpMethod.POST, entity, String.class);
    }

    /**
     * Call /api/captures/record POST endpoint.
     */
    private ResponseEntity<String> recordVideo() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/api/captures/record"), HttpMethod.POST, entity, String.class);
    }

    /**
     * Call /api/captures/stop POST endpoint.
     */
    private ResponseEntity<String> stopVideo() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/api/captures/stop"), HttpMethod.POST, entity, String.class);
    }

    /**
     * Call /api/captures/finish POST endpoint.
     */
    private ResponseEntity<String> finishVideo(CaptureFinishParams captureFinishParams) {
        HttpEntity<CaptureFinishParams> entity = new HttpEntity<>(captureFinishParams, headers);
        return restTemplate.exchange(
            urlWithPort("/api/captures/finish"), HttpMethod.POST, entity, String.class);
    }

    /**
     * Call /api/captures/cancel POST endpoint.
     */
    private ResponseEntity<String> cancelVideo() {
        HttpEntity<CaptureFinishParams> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/api/captures/cancel"), HttpMethod.POST, entity, String.class);
    }

    /**
     * Call /api/captures/status GET endpoint.
     */
    private ResponseEntity<String> statusVideo() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/api/captures/status"), HttpMethod.GET, entity, String.class);
    }

    /**
     * Call /api/captures/[captureId] DELETE endpoint.
     */
    private ResponseEntity<Void> deleteVideo(String captureId) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate
            .exchange(urlWithPort("/api/captures/" + captureId), HttpMethod.DELETE, entity, Void.class);
    }

    /**
     * Call /api/captures/upload/[captureId] endpoint.
     */
    private ResponseEntity<String> uploadById(String captureId) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/api/captures/upload/" + captureId), HttpMethod.POST, entity, String.class);
    }

    /**
     * Call /api/captures/upload endpoint.
     */
    private ResponseEntity<String> uploadStatus() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate
            .exchange(urlWithPort("/api/captures/upload"), HttpMethod.GET, entity, String.class);
    }

    /**
     * Grabs the first `state` in the status array.
     */
    private String uploadStatusState() {
        ResponseEntity<String> response = uploadStatus();
        String state = JsonPath.parse(response.getBody()).read("$[0].state");
        return state;
    }

    /**
     * Cancel and assert that the status is idle.
     */
    private void cancelAndAssertStatusIsIdle(String initialCheck, ResponseEntity<String> response)
        throws JSONException {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsString(initialCheck).isNotNull();

        cancelVideo();

        response = statusVideo();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("{'state' : 'idle'}", response.getBody(), true);
    }

}
