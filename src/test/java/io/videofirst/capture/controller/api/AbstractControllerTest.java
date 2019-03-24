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

import static org.mockito.Mockito.mock;

import io.videofirst.capture.IntegrationTest;
import io.videofirst.capture.VfCapture;
import io.videofirst.capture.configuration.HeadlessSpringApplicationContextLoader;
import io.videofirst.capture.configuration.properties.UploadConfig;
import io.videofirst.capture.controller.api.AbstractControllerTest.IntegrationTestContextConfiguration;
import io.videofirst.capture.mock.MockUploadController.MockUploadService;
import io.videofirst.capture.service.CaptureService;
import io.videofirst.capture.service.impl.DefaultInfoService;
import io.videofirst.capture.service.impl.DefaultUploadService;
import io.videofirst.capture.test.VfCaptureTesting;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Abstract class which all integration level tests should extend.
 *
 * @author Bob Marks
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = VfCapture.class, loader = HeadlessSpringApplicationContextLoader.class)
@ActiveProfiles(profiles = {"test", "integration-test"})
@Category(value = IntegrationTest.class)
@Import(IntegrationTestContextConfiguration.class)
public abstract class AbstractControllerTest {

    private static final int DEFAULT_PORT = 1357;

    @TestConfiguration
    static class IntegrationTestContextConfiguration {

        @Bean
        public MockUploadService getMockedUploadService() {
            return mock(MockUploadService.class);
        }

    }

    // Autowired fields

    @LocalServerPort
    protected int port;

    @Autowired
    protected CaptureService captureService;

    @Autowired
    protected MockUploadService mockUploadService;

    @Autowired
    protected DefaultUploadService uploadService;

    @Autowired
    protected DefaultInfoService infoService;

    // Internal fields

    protected HttpHeaders headers;

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    @Before
    public void setUp() throws Exception {
        VfCaptureTesting.initTestFolders();

        headers = new HttpHeaders();
        headers.add("Authorization", "Basic dGVzdDpwYXNzd29yZA=="); // test:password

        // Override upload URL port as this is only known run-time of integration test.
        UploadConfig uploadConfig = uploadService.getUploadConfig();
        uploadConfig.setUrl(uploadConfig.getUrl().replace("" + DEFAULT_PORT, "" + port));
    }

    @After
    public void tearDown() throws Exception {
        captureService.cancel(); // Always cancel after every integration test
        uploadService.cancel();
        VfCaptureTesting.cleanTestFolders(); // clean out any videos generated
    }

    protected String urlWithPort(final String url) {
        return "http://localhost:" + port + url;
    }

}