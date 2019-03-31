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

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.revinate.assertj.json.JsonPathAssert;
import java.util.Map;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Integration test to test the REST methods of InfoController.
 *
 * @author Bob Marks
 */
public class InfoControllerTest extends AbstractControllerTest {

    // ===========================================
    // [ / ] GET
    // ===========================================

    @Test
    public void shouldRetrieveInfo() throws JSONException {

        ResponseEntity<String> response = restTemplate.exchange(
            urlWithPort("/"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String expectedJson = "{" +
            "  'info': {" +
            "    'storage': {}," +
            "    'upload': {" +
            "      'enable': true," +
            "      'url': 'http://localhost:" + port + "/mock-upload'," +
            "      'headers': {" +
            "        'Authorization': '########'" +
            "      }," +
            "      'threads': 1," +
            "      'keepFinishedUploadsInSecs': 30" +
            "    }," +
            "    'display': {" +
            "      'x': 0," +
            "      'y': 0" +
            "    }," +
            "    'environment': {}" +
            "  }," +
            "  'defaults': {" +
            "    'project': 'Moon Rocket'," +
            "    'display': {" +
            "      'alwaysOnTop': 'false'," +
            "      'capture': {" +
            "        'x': '0'," +
            "        'y': '0'," +
            "        'width': 'displayWidth'," +
            "        'height': 'displayHeight'" +
            "      }," +
            "      'border': {" +
            "        'display': 'false'," +
            "        'color': '#ff0000'," +
            "        'padding': '1'," +
            "        'width': '1'" +
            "      }," +
            "      'background': {" +
            "        'display': 'false'," +
            "        'color': '#ffffff'," +
            "        'x': '0'," +
            "        'y': '0'," +
            "        'width': 'displayWidth'," +
            "        'height': 'displayHeight'" +
            "      }," +
            "      'text': {" +
            "        'display': 'false'," +
            "        'color': '#000000'," +
            "        'fontSize': '12'," +
            "        'x': '0'," +
            "        'y': '0'" +
            "      }" +
            "    }" +
            "  }," +
            "  'captureStatus': {" +
            "    'recording': false" +
            "  }" +
            "}";
        JSONAssert.assertEquals(expectedJson, response.getBody(), false);
        DocumentContext json = JsonPath.parse(response.getBody());

        JsonPathAssert.assertThat(json).jsonPathAsString("$.info.started").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.info.uptimeSeconds")
            .isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.info.storage.tempFolder")
            .isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.info.storage.videoFolder")
            .isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.info.display.width").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.info.display.height").isNotNull();
        JsonPathAssert.assertThat(json).jsonPathAsString("$.info.display.height").isNotNull();
        Map<String, String> environmentMap = json.read("$.info.environment");
        assertThat(environmentMap.get("java.awt.graphicsenv")).isNotEmpty();
    }

}
