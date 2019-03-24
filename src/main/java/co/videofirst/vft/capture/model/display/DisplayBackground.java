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
package co.videofirst.vft.capture.model.display;

import co.videofirst.vft.capture.configuration.properties.DisplayBackgroundConfig;
import co.videofirst.vft.capture.exception.VideoConfigurationException;
import co.videofirst.vft.capture.model.info.DisplayInfo;
import co.videofirst.vft.capture.utils.ConfigUtils;
import java.awt.Color;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Display background class.
 *
 * @author Bob Marks
 */
@Data
@NoArgsConstructor
public class DisplayBackground extends AbstractDisplayRectangle {

    // Constants

    public static final boolean DEFAULT_BACKGROUND_DISPLAY = false;
    public static final Color DEFAULT_BACKGROUND_COLOR = new Color(255, 255, 255);

    private boolean display;

    private Color color;

    @Builder
    private DisplayBackground(int x, int y, int width, int height, boolean display, Color color) {
        super(x, y, width, height);
        this.display = display;
        this.color = color;
    }

    public static DisplayBackground build(DisplayInfo displayInfo,
        DisplayBackgroundConfig baseBackgroundConfig,
        DisplayBackgroundConfig videoBackgroundConfig,
        Map<String, Integer> expressionMap) {

        if (baseBackgroundConfig == null) {
            baseBackgroundConfig = new DisplayBackgroundConfig();
        }
        if (videoBackgroundConfig == null) {
            videoBackgroundConfig = new DisplayBackgroundConfig();
        }

        int x = parseExpInts(baseBackgroundConfig.getX(), videoBackgroundConfig.getX(),
            displayInfo.getX(), expressionMap, false, null);
        int y = parseExpInts(baseBackgroundConfig.getY(), videoBackgroundConfig.getY(),
            displayInfo.getY(), expressionMap, false, null);
        int width = parseExpInts(baseBackgroundConfig.getWidth(), videoBackgroundConfig.getWidth(),
            displayInfo.getWidth(), expressionMap, true,
            "Invalid width [ %n ] for background area - cannot be a negative number");
        int height = parseExpInts(baseBackgroundConfig.getHeight(),
            videoBackgroundConfig.getHeight(),
            displayInfo.getHeight(), expressionMap, true,
            "Invalid height [ %n ] for background area - cannot be a negative number");
        boolean display = ConfigUtils
            .parseBooleans(baseBackgroundConfig.getDisplay(), videoBackgroundConfig.getDisplay(),
                DEFAULT_BACKGROUND_DISPLAY);
        Color color = ConfigUtils
            .parseColors(baseBackgroundConfig.getColor(), videoBackgroundConfig.getColor(),
                DEFAULT_BACKGROUND_COLOR);

        DisplayBackground displayBackground = DisplayBackground.builder()
            .x(x).y(y).width(width).height(height).display(display).color(color).build();

        return displayBackground;
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
