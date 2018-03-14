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

import co.videofirst.vft.capture.enums.UploadState;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Video upload object.
 *
 * @author Bob Marks
 */
@Data
@Builder(toBuilder = true)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"state", "url", "scheduled", "started", "updated", "finished", "total",
    "transferred", "errorMessage", "statusCode"})
public class Upload {

    private UploadState state;
    private String url;
    private LocalDateTime scheduled;
    private LocalDateTime started;
    private LocalDateTime updated;
    private LocalDateTime finished;
    private Long total;
    private Long transferred;
    private String errorMessage;
    private Integer statusCode;

    public static Upload schedule(String url) {
        Upload upload = Upload.builder()
            .state(UploadState.scheduled)
            .url(url)
            .scheduled(LocalDateTime.now())
            .build();
        return upload;
    }

    public Upload start() {
        Upload upload = toBuilder()
            .state(UploadState.uploading)
            .started(LocalDateTime.now())
            .build();
        return upload;
    }

    public Upload updateProgress(long transferredBytes, long totalBytes) {
        Upload upload = toBuilder()
            .state(UploadState.uploading)
            .transferred(transferredBytes)
            .started(LocalDateTime.now())
            .total(totalBytes)
            .build();
        return upload;
    }

    public Upload finish() {
        Upload upload = toBuilder()
            .state(UploadState.finished)
            .transferred(total) // just make transferred the same as total
            .updated(LocalDateTime.now())
            .finished(LocalDateTime.now())
            .build();
        return upload;
    }

    public Upload error(String errorMessage, Integer statusCode) {
        Upload upload = toBuilder()
            .state(UploadState.error)
            .updated(LocalDateTime.now())
            .errorMessage(errorMessage)
            .statusCode(statusCode)
            .build();
        return upload;
    }

}
