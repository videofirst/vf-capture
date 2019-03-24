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

import io.videofirst.capture.configuration.properties.DisplayTextConfig;
import io.videofirst.capture.exception.VideoConfigurationException;
import io.videofirst.capture.model.info.DisplayInfo;
import io.videofirst.capture.utils.ConfigUtils;
import java.awt.Color;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Display text object.
 *
 * @author Bob Marks
 */
@Data
@Builder
public class DisplayText {

    public static final boolean DEFAULT_TEXT_DISPLAY = false;
    public static final Color DEFAULT_TEXT_COLOR = new Color(0, 0, 0);
    public static final Integer DEFAULT_TEXT_FONT_SIZE = 12;

    private boolean display;
    private Color color;
    private int fontSize;
    private int x;
    private int y;

    /**
     * Build object from displayInfo, baseTextConfig,
     */
    public static DisplayText build(DisplayInfo displayInfo, DisplayTextConfig baseTextConfig,
        DisplayTextConfig overrideTextConfig, Map<String, Integer> expressionMap) {

        if (baseTextConfig == null) {
            baseTextConfig = new DisplayTextConfig();
        }
        if (overrideTextConfig == null) {
            overrideTextConfig = new DisplayTextConfig();
        }
        boolean display = ConfigUtils
            .parseBooleans(baseTextConfig.getDisplay(), overrideTextConfig.getDisplay(),
                DEFAULT_TEXT_DISPLAY);
        Color color = ConfigUtils
            .parseColors(baseTextConfig.getColor(), overrideTextConfig.getColor(),
                DEFAULT_TEXT_COLOR);
        int fontSize = parseExpInts(baseTextConfig.getFontSize(), overrideTextConfig.getFontSize(),
            DEFAULT_TEXT_FONT_SIZE, expressionMap, true,
            "Invalid width [ %n ] for capture area - cannot be a negative number");
        int x = parseExpInts(baseTextConfig.getX(), overrideTextConfig.getX(),
            displayInfo.getX(), expressionMap, false, null);
        int y = parseExpInts(baseTextConfig.getY(), overrideTextConfig.getY(),
            displayInfo.getY(), expressionMap, false, null);

        DisplayText displayCapture = DisplayText.builder()
            .display(display).color(color).fontSize(fontSize).x(x).y(y).build();
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
