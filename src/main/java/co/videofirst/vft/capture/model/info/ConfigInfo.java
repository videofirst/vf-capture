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
package co.videofirst.vft.capture.model.info;

import co.videofirst.vft.capture.configuration.properties.StorageConfig;
import co.videofirst.vft.capture.configuration.properties.UploadConfig;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Current view of configuration / system.
 *
 * @author Bob Marks
 */
@Data
@Builder
@JsonPropertyOrder({"started", "uptimeSeconds", "categories", "storage", "upload", "display",
    "environment"})
public class ConfigInfo {

    private LocalDateTime started;
    private List<String> categories;
    private StorageConfig storage;
    private UploadConfig upload;
    private DisplayInfo display;
    private Map<String, String> environment;

    public long getUptimeSeconds() {
        return started == null ? 0 : started.until(LocalDateTime.now(), ChronoUnit.SECONDS);
    }

}
