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

/**
 * Immutable upload status transport object.
 *
 * @author Bob Marks
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"id", "state", "url", "scheduled", "started", "updated", "finished", "total",
    "transferred", "error"})
public class UploadStatus {

    private final Video video;
    private final Upload upload;

    public UploadStatus(final Video video) {
        this.video = video;
        upload = video.getUpload();
    }

    public String getId() {
        return video.getId();
    }

    public UploadState getState() {
        return upload.getState();
    }

    public String getUrl() {
        return upload.getUrl();
    }

    public LocalDateTime getScheduled() {
        return upload.getScheduled();
    }

    public LocalDateTime getStarted() {
        return upload.getStarted();
    }

    public LocalDateTime getUpdated() {
        return upload.getUpdated();
    }

    public LocalDateTime getFinished() {
        return upload.getFinished();
    }

    public Long getTotal() {
        return upload.getTotal();
    }

    public Long getTransferred() {
        return upload.getTransferred();
    }

    public String getErrorMessage() {
        return upload.getErrorMessage();
    }

    public Integer getStatusCode() {
        return upload.getStatusCode();
    }

    public void setState(UploadState state) {
        upload.setState(state);
    }

    public void setUrl(String url) {
        upload.setUrl(url);
    }
}
