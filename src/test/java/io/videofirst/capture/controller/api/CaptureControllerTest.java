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
package io.videofirst.capture.controller.api;

import static io.videofirst.capture.enums.LogTier.L1;
import static io.videofirst.capture.enums.LogTier.L2;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.revinate.assertj.json.JsonPathAssert;
import io.videofirst.capture.enums.CaptureType;
import io.videofirst.capture.enums.TestStatus;
import io.videofirst.capture.model.TestLog;
import io.videofirst.capture.model.capture.CaptureRecordParams;
import io.videofirst.capture.model.capture.CaptureStopParams;
import io.videofirst.capture.test.VfCaptureTesting;
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

    private static final CaptureRecordParams CAPTURE_RECORD_PARAMS_NONE = CaptureRecordParams
        .builder()
        .build();
    private static final CaptureRecordParams CAPTURE_RECORD_PARAMS_MIN = CaptureRecordParams
        .builder()
        .feature("Bob Feature").scenario("Dave Scenario").build();
    private static final CaptureRecordParams CAPTURE_RECORD_PARAMS_MAX = CaptureRecordParams
        .builder()
        .project(" Google Search ")
        .feature(" Advanced Search ")
        .scenario(" Search by Country! ")
        .sid(5678L)
        .type(CaptureType.automated)
        .meta(DEFAULT_META)
        .description("Awesome test")
        .build();

    // Capture finish params

    private static final CaptureStopParams CAPTURE_STOP_PARAMS_MIN = CaptureStopParams
        .builder().build();
    private static final CaptureStopParams CAPTURE_STOP_PARAMS_MAX = CaptureStopParams
        .builder()
        .testStatus(TestStatus.fail)
        .meta(ImmutableMap.of("author", "Bob", "extra", "stuff"))
        .description(" even more awesome description ")
        .error(" awesome error ")
        .stackTrace(
            "io.videofirst.capture.exception.InvalidStateException: Current state is idle")
        .logs(asList(
            TestLog.builder().cat("browser").tier(L1).ts(TS1).log("awesome log 1").build(),
            TestLog.builder().cat("server").tier(L2).ts(TS2).log("awesome log 2").build()))
        .build();

    // ===========================================
    // [ /captures ] GET
    // ===========================================

    @Test
    public void shouldRetrieveCaptures() throws JSONException {

        ResponseEntity<String> response = videos();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "[{" +
            "    'id': '2018-02-23_10-13-25_9ip93m'," +
            "    'type': 'manual'," +
            "    'project': 'Google Search'," +
            "    'feature': 'Home Page Search'," +
            "    'scenario': 'Property Search in Belfast'," +
            "    'started': '2018-02-23T10:13:25.256'," +
            "    'finished': '2018-02-23T10:13:30.184'," +
            "    'format': 'avi'," +
            "    'testStatus': 'pass'" +
            "}, {" +
            "    'id': '2018-02-15_12-14-02_n3jwzb'," +
            "    'sid': 1234," +
            "    'type': 'automated'," +
            "    'project': 'Moon Rocket'," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'," +
            "    'started': '2018-02-15T12:14:02'," +
            "    'finished': '2018-02-15T12:14:03'," +
            "    'format': 'avi'," +
            "    'testStatus': 'fail'" +
            "}]";
        JSONAssert.assertEquals(expectedJson, response.getBody(), true);
    }

    // Private methods

    /**
     * Call /captures GET endpoint.
     */
    private ResponseEntity<String> videos() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate
            .exchange(urlWithPort("/captures"), HttpMethod.GET, entity, String.class);
    }

    // ===========================================
    // [ /captures/<captureId> ] GET
    // ===========================================

    @Test
    public void shouldRetrieveCaptureById() throws JSONException {

        ResponseEntity<String> response = video(MOCK_CAPTURE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'id': '2018-02-15_12-14-02_n3jwzb'," +
            "    'type': 'automated'," +
            "    'sid': 1234," +
            "    'project': 'Moon Rocket'," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'," +
            "    'started': '2018-02-15T12:14:02'," +
            "    'finished': '2018-02-15T12:14:03'," +
            "    'folder': 'moon-rocket/bob-feature/dave-scenario/2018-02-15_12-14-02_n3jwzb',"
            +
            "    'format': 'avi'," +
            "    'capture': {" +
            "        'x': 0," +
            "        'y': 0," +
            "        'width': 1920," +
            "        'height': 1200" +
            "    }," +
            "    'meta': {}," +
            "    'environment': {" +
            "        'java.awt.graphicsenv': 'sun.awt.Win32GraphicsEnvironment'" +
            "    }," +
            "    'testStatus': 'fail'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), true);
    }

    // Private methods

    /**
     * Call /captures/[captureId] GET endpoint.
     */
    private ResponseEntity<String> video(String captureId) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/captures/" + captureId), HttpMethod.GET, entity, String.class);
    }

    // ===========================================
    // [ /captures/record ] POST
    // ===========================================

    @Test
    public void shouldRecordNoParams() throws JSONException {

        ResponseEntity<String> response = recordVideo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'type': 'manual'," +
            "    'recording': true," +
            "    'project': 'Moon Rocket'," +
            "    'format': 'avi'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsString("$.started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.durationSeconds").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.folder")
            .matches("moon-rocket/\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    @Test
    public void shouldRecordBlankParams() throws JSONException {

        ResponseEntity<String> response = recordVideo(CAPTURE_RECORD_PARAMS_NONE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'type': 'manual'," +
            "    'recording': true," +
            "    'project': 'Moon Rocket'," +
            "    'format': 'avi'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        JsonPathAssert.assertThat(json).jsonPathAsString("$.started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.durationSeconds").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.folder")
            .matches("moon-rocket/\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[a-z0-9]{6}");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    @Test
    public void shouldRecordMinParams() throws JSONException {

        ResponseEntity<String> response = recordVideo(CAPTURE_RECORD_PARAMS_MIN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'type': 'manual'," +
            "    'recording': true," +
            "    'project': 'Moon Rocket'," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    @Test
    public void shouldFailRecordWithNoProject() throws JSONException {
        String project = infoService.getInfo().getDefaults().getProject();
        infoService.getInfo().getDefaults().setProject(""); // remove project for this test

        ResponseEntity<String> response = recordVideo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        infoService.getInfo().getDefaults().setProject(project);  // put project back in again
    }

    @Test
    public void shouldRecordWithMaximumParams() throws JSONException {

        ResponseEntity<String> response = recordVideo(CAPTURE_RECORD_PARAMS_MAX);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'type': 'automated'," +
            "    'sid': 5678," +
            "    'recording': true," +
            "    'project': 'Google Search'," +
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
        JsonPathAssert.assertThat(json)
            .jsonPathAsString("$.folder") // 5678/2019-03-31_13-44-03_s6tkwo
            .matches("^5678/\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[\\w\\d]{6}$");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    @Test
    public void shouldRecordWhenAlreadyStartedUsingForce() throws JSONException {

        recordVideo(CAPTURE_RECORD_PARAMS_MIN);
        ResponseEntity<String> response = recordVideo(CaptureRecordParams.builder()
            .feature("Bob Feature").scenario("Dave Scenario").force("true").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("{'recording': true}", response.getBody(), false);
    }

    // Private methods

    private ResponseEntity<String> recordVideo() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/captures/record"), HttpMethod.POST, entity, String.class);
    }

    private ResponseEntity<String> recordVideo(CaptureRecordParams captureRecordParams) {
        HttpEntity<CaptureRecordParams> entity = new HttpEntity<>(captureRecordParams, headers);
        return restTemplate.exchange(
            urlWithPort("/captures/record"), HttpMethod.POST, entity, String.class);
    }

    // ===========================================
    // [ /captures/stop ] POST
    // ===========================================

    @Test
    public void shouldStopNoParams() throws JSONException {

        recordVideo(CAPTURE_RECORD_PARAMS_MIN);
        ResponseEntity<String> response = stopVideo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'type': 'manual'," +
            "    'recording': false," +
            "    'project': 'Moon Rocket'," +
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
            .startsWith("moon-rocket/bob-feature/dave-scenario/");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    @Test
    public void shouldStopWithMinParams() throws JSONException {

        recordVideo(CAPTURE_RECORD_PARAMS_MIN);
        ResponseEntity<String> response = stopVideo(CAPTURE_STOP_PARAMS_MIN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'type': 'manual'," +
            "    'recording': false," +
            "    'project': 'Moon Rocket'," +
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
            .startsWith("moon-rocket/bob-feature/dave-scenario/");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    @Test
    public void shouldStopWithMaxParams() throws JSONException {

        recordVideo(CAPTURE_RECORD_PARAMS_MAX);
        ResponseEntity<String> response = stopVideo(CAPTURE_STOP_PARAMS_MAX);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'type': 'automated'," +
            "    'sid': 5678," +
            "    'recording': false," +
            "    'project': 'Google Search'," +
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
            "    'testStackTrace': 'io.videofirst.capture.exception.InvalidStateException: Current state is idle',"
            +
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
            .matches("^5678/\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_[\\w\\d]{6}$");
        JsonPathAssert.assertThat(json).jsonPathAsString("$.id").isNotNull().hasSize(26);
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.x").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.y").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.capture.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    // Private methods

    private ResponseEntity<String> stopVideo() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/captures/stop"), HttpMethod.POST, entity, String.class);
    }

    private ResponseEntity<String> stopVideo(CaptureStopParams captureStopParams) {
        HttpEntity<CaptureStopParams> entity = new HttpEntity<>(captureStopParams, headers);
        return restTemplate.exchange(
            urlWithPort("/captures/stop"), HttpMethod.POST, entity, String.class);
    }

    // ===========================================
    // [ /captures/status ] GET
    // ===========================================

    @Test
    public void shouldGetsStatusWithStartedUsingMinParams() throws JSONException {

        recordVideo(CAPTURE_RECORD_PARAMS_MIN);
        ResponseEntity<String> response = statusVideo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "    'type': 'manual'," +
            "    'recording': true," +
            "    'project': 'Moon Rocket'," +
            "    'feature': 'Bob Feature'," +
            "    'scenario': 'Dave Scenario'" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());
        Map<String, String> environmentMap = json.read("$.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

    // Private methods

    private ResponseEntity<String> statusVideo() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/captures/status"), HttpMethod.GET, entity, String.class);
    }

    // ===========================================
    // [ /captures/<captureId> ] DELETE
    // ===========================================

    @Test
    public void shouldDeleteVideo() {

        // Check video exists
        File folder = new File(VfCaptureTesting.VF_VIDEO_FOLDER,
            "moon-rocket/bob-feature/dave-scenario/2018-02-15_12-14-02_n3jwzb");
        File dataFile = new File(folder, MOCK_CAPTURE_ID + ".json");
        File videoFile = new File(folder, MOCK_CAPTURE_ID + ".avi");
        assertThat(dataFile).exists();
        assertThat(videoFile).exists();

        assertThat(deleteVideo(MOCK_CAPTURE_ID).getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(dataFile).doesNotExist();
        assertThat(videoFile).doesNotExist();
    }

    // Private methods

    private ResponseEntity<Void> deleteVideo(String captureId) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate
            .exchange(urlWithPort("/captures/" + captureId), HttpMethod.DELETE, entity,
                Void.class);
    }

    // ===========================================
    // [ /captures/cancel ]
    // ===========================================

    @Test
    public void shouldCancelFromIdleState() throws JSONException {
        ResponseEntity<String> response = statusVideo();

        cancelAndAssertStatusIsIdle(null, response);
    }

    @Test
    public void shouldCancelFromStartedState() throws JSONException {
        ResponseEntity<String> response = recordVideo(CAPTURE_RECORD_PARAMS_MIN);

        cancelAndAssertStatusIsIdle("$.feature", response);
    }

    @Test
    public void shouldCancelFromRecordingState() throws JSONException {
        ResponseEntity<String> response = recordVideo(CAPTURE_RECORD_PARAMS_MIN);

        cancelAndAssertStatusIsIdle("$.started", response);
    }

    @Test
    public void shouldCancelFromStoppedState() throws JSONException {
        recordVideo(CAPTURE_RECORD_PARAMS_MIN);
        ResponseEntity<String> response = stopVideo();

        cancelAndAssertStatusIsIdle("$.finished", response);
    }

    @Test
    public void shouldCancelFromFinishedState() throws JSONException {
        recordVideo(CAPTURE_RECORD_PARAMS_MIN);
        ResponseEntity<String> response = stopVideo(CAPTURE_STOP_PARAMS_MIN);

        cancelAndAssertStatusIsIdle("$.finished", response);
    }

    // Private methods

    private void cancelAndAssertStatusIsIdle(String initialCheck, ResponseEntity<String> response)
        throws JSONException {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext json = JsonPath.parse(response.getBody());
        if (initialCheck != null) {
            JsonPathAssert.assertThat(json).jsonPathAsString(initialCheck).isNotNull();
        }

        cancelVideo();

        response = statusVideo();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("{'recording' : false}", response.getBody(), true);
    }

    private ResponseEntity<String> cancelVideo() {
        HttpEntity<CaptureStopParams> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/captures/cancel"), HttpMethod.POST, entity, String.class);
    }

    // ===========================================
    // [ /captures/upload ]
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

    private ResponseEntity<String> uploadById(String captureId) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            urlWithPort("/captures/upload/" + captureId), HttpMethod.POST, entity,
            String.class);
    }

    private ResponseEntity<String> uploadStatus() {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate
            .exchange(urlWithPort("/captures/upload"), HttpMethod.GET, entity, String.class);
    }

    private String uploadStatusState() {
        ResponseEntity<String> response = uploadStatus();
        String state = JsonPath.parse(response.getBody()).read("$[0].state");
        return state;
    }

}
