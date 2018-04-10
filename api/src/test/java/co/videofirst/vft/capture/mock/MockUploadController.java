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
package co.videofirst.vft.capture.mock;

import co.videofirst.vft.capture.http.ProgressEntityWrapper;
import co.videofirst.vft.capture.http.ProgressListener;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Mock upload controller which can be used to ensure that an upload occurred.
 *
 * @author Bob Marks
 */
@RestController
@RequestMapping("/mock-upload")
@RequiredArgsConstructor
@Profile("integration-test") // only load when we're integration testing
public class MockUploadController {

    /**
     * Use this little static interface to ensure that the uploaded video got mocked out.
     */
    public interface MockUploadService {

        void upload(MultipartFile videoFile, MultipartFile dataFile);

    }

    private final MockUploadService uploadService;

    @PostMapping
    public ResponseEntity<Void> uploadVideo(@RequestParam("video") MultipartFile videoFile,
        @RequestParam("data") MultipartFile dataFile) {
        uploadService.upload(videoFile, dataFile);
        return ResponseEntity.ok().build();
    }

    /**
     * Manually hit an end-point.
     */
    public static void main(String[] args) throws IOException {

        String dir = "src/test/resources/videos/acme/moon-rocket/ui/bob-feature/dave-scenario/2018-02-15_12-14-02_n3jwzb/";
        File videoFile = new File(dir + "2018-02-15_12-14-02_n3jwzb.avi");
        File dataFile = new File(dir + "2018-02-15_12-14-02_n3jwzb.json");

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:1357/mock-upload");
        httpPost.setHeader("Authorization", "Basic dGVzdDpwYXNzd29yZA==");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("video", videoFile, ContentType.DEFAULT_BINARY, videoFile.getName());
        builder.addBinaryBody("data", dataFile, ContentType.DEFAULT_BINARY, dataFile.getName());
        HttpEntity multipart = builder.build();

        ProgressListener pListener = (transferredBytes, totalBytes) -> System.out.println(
            "transferredBytes [ " + transferredBytes + " ] , totalBytes [ " + totalBytes + " ] ");

        httpPost.setEntity(new ProgressEntityWrapper(multipart, pListener));

        System.out.println("Posting");
        CloseableHttpResponse response = client.execute(httpPost);
        System.out.println(response.getStatusLine().getStatusCode());
        client.close();
        System.out.println("Done");
    }

}
