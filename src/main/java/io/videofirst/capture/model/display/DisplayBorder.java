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

import io.videofirst.capture.configuration.properties.DisplayBorderConfig;
import io.videofirst.capture.utils.ConfigUtils;
import java.awt.Color;
import lombok.Builder;
import lombok.Data;

/**
 * Display border class.
 *
 * @author Bob Marks
 */
@Data
@Builder
public class DisplayBorder {

    // Constants

    public static final boolean DEFAULT_BORDER_DISPLAY = false;
    public static final Color DEFAULT_BORDER_COLOR = new Color(255, 0, 0);
    public static final Integer DEFAULT_PADDING = 1;
    public static final Integer DEFAULT_WIDTH = 1;

    // Fields

    private boolean display;
    private Color color;
    private int padding;
    private int width;

    public static DisplayBorder build(DisplayBorderConfig baseBorderConfig,
        DisplayBorderConfig videoBorderConfig) {

        if (baseBorderConfig == null) {
            baseBorderConfig = new DisplayBorderConfig();
        }
        if (videoBorderConfig == null) {
            videoBorderConfig = new DisplayBorderConfig();
        }

        // Parse values from (1) video border config, then (2) base border config.
        boolean display = ConfigUtils
            .parseBooleans(baseBorderConfig.getDisplay(), videoBorderConfig.getDisplay(),
                DEFAULT_BORDER_DISPLAY);
        Color color = ConfigUtils
            .parseColors(baseBorderConfig.getColor(), videoBorderConfig.getColor(),
                DEFAULT_BORDER_COLOR);
        int padding = ConfigUtils
            .parseInts(baseBorderConfig.getPadding(), videoBorderConfig.getPadding(),
                DEFAULT_PADDING);
        int width = ConfigUtils
            .parseInts(baseBorderConfig.getWidth(), videoBorderConfig.getWidth(), DEFAULT_WIDTH);

        return DisplayBorder.builder().display(display).color(color).padding(padding).width(width)
            .build();
    }

}
