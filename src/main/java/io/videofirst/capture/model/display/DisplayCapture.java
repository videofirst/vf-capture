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
package io.videofirst.capture.model.display;

import io.videofirst.capture.configuration.properties.DisplayCaptureConfig;
import io.videofirst.capture.exception.VideoConfigurationException;
import io.videofirst.capture.model.info.DisplayInfo;
import io.videofirst.capture.utils.ConfigUtils;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Display capture class.
 *
 * @author Bob Marks
 */
@Data
@NoArgsConstructor
public class DisplayCapture extends AbstractDisplayRectangle {

    @Builder
    private DisplayCapture(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * Build object from displayInfo, capture and
     */
    public static DisplayCapture build(DisplayInfo displayInfo,
        DisplayCaptureConfig baseCaptureConfig,
        DisplayCaptureConfig videoCaptureConfig,
        Map<String, Integer> expressionMap) {

        if (baseCaptureConfig == null) {
            baseCaptureConfig = new DisplayCaptureConfig();
        }
        if (videoCaptureConfig == null) {
            videoCaptureConfig = new DisplayCaptureConfig();
        }

        int x = parseExpInts(baseCaptureConfig.getX(), videoCaptureConfig.getX(),
            displayInfo.getX(), expressionMap, false, null);
        int y = parseExpInts(baseCaptureConfig.getY(), videoCaptureConfig.getY(),
            displayInfo.getY(), expressionMap, false, null);
        int width = parseExpInts(baseCaptureConfig.getWidth(), videoCaptureConfig.getWidth(),
            displayInfo.getWidth(), expressionMap, true,
            "Invalid width [ %n ] for capture area - cannot be a negative number");
        int height = parseExpInts(baseCaptureConfig.getHeight(), videoCaptureConfig.getHeight(),
            displayInfo.getHeight(), expressionMap, true,
            "Invalid height [ %n ] for capture area - cannot be a negative number");

        DisplayCapture displayCapture = DisplayCapture.builder()
            .x(x).y(y).width(width).height(height).build();
        return displayCapture;
    }

    // Private methods

    private static int parseExpInts(String baseInt, String overrideInt, int defaultInt,
        Map<String, Integer> expParams, boolean checkForNegativeNumber, String message) {
        int integer = ConfigUtils.parseExpInts(baseInt, overrideInt, defaultInt, expParams);
        if (checkForNegativeNumber && integer < 0) {
            throw new VideoConfigurationException(String.format(message, integer));
        }
        return integer;
    }

}