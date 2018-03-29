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
package co.videofirst.vft.capture.model.capture;

import co.videofirst.vft.capture.configuration.properties.DisplayConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * All these attributes can be overriden at run-time i.e. when creating a capture.
 *
 * @author Bob Marks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptureStartParams {

    // Mandatory

    @JsonProperty(required = true)
    @ApiModelProperty(notes = "Feature e.g. \"Search\"", required = true, position = 1)
    private String feature;

    @JsonProperty(required = true)
    @ApiModelProperty(notes = "Scenario of the feature e.g. \"Search by Country\"", required = true, position = 2)
    private String scenario;

    // Optional

    @ApiModelProperty(notes = "Categories map of string keys / string values", position = 3)
    private Map<String, String> categories;

    @ApiModelProperty(notes = "Optional start record (defaults to \"true\")", allowableValues = "false,true", position = 4)
    private String record;

    @ApiModelProperty(notes = "Force a start (will cancel existing recordings) if not in an idle state (defaults to \"false\")", allowableValues = "false,true", position = 5)
    private String force;

    @ApiModelProperty(notes = "Optional meta tags e.g. {\"version\" : \"0.12.4\"}", position = 6)
    private Map<String, String> meta;

    @ApiModelProperty(notes = "Optional description of test", position = 7)
    private String description;

    @ApiModelProperty(notes = "Optional display overrides e.g. for setting borders, backgrounds, etc", position = 8)
    private DisplayConfig display;

    public boolean record() {
        return record == null || "true".equalsIgnoreCase(record.trim());
    }

    public boolean force() {
        return force != null && "true".equalsIgnoreCase(force.trim());
    }

}
