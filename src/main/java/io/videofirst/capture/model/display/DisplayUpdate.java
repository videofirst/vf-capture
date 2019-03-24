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

import io.videofirst.capture.configuration.properties.DisplayConfig;
import io.videofirst.capture.model.info.DisplayInfo;
import io.videofirst.capture.model.info.Info;
import io.videofirst.capture.utils.ConfigUtils;
import io.videofirst.capture.utils.ExpressionUtils;
import lombok.Builder;
import lombok.Value;

/**
 * Immutable object which has enough information to update display.
 *
 * @author Bob Marks
 */
@Value
@Builder
public class DisplayUpdate {

    // Constants

    private static final boolean DEFAULT_ALWAYS_ON_TOP = false;
    private static final int DEFAULT_SCREEN = 1;

    // Fields

    private int screen;
    private boolean alwaysOnTop;

    private DisplayCapture capture;
    private DisplayBackground background;
    private DisplayBorder border;
    private DisplayText text;

    /**
     * If all fields are null then we don't need to display anything.
     */
    public boolean noDisplayUpdates() {
        return background == null && border == null && text == null;
    }

    /**
     * Create a display update object from the base info and
     */
    public static DisplayUpdate build(Info info, DisplayConfig displayConfig) {

        // 1) Retrieve info on the display
        if (info == null || info.getInfo() == null || info.getInfo().getDisplay() == null) {
            return DisplayUpdate.builder().build();  // blank object
        }
        DisplayInfo displayInfo = info.getInfo().getDisplay();

        // 2) Retrieve base config (or create a new one if it doesn't exist)s
        DisplayConfig baseDisplayConfig =
            info.getDefaults() != null && info.getDefaults().getDisplay() != null ?
                info.getDefaults().getDisplay() : new DisplayConfig();

        // 3) If videoConfig is null then create a blank one
        if (displayConfig == null) {
            displayConfig = new DisplayConfig();
        }

        // primitives
        int screen = ConfigUtils
            .parseInts(baseDisplayConfig.getScreen(), displayConfig.getScreen(),
                DEFAULT_SCREEN);
        boolean alwaysOnTop = ConfigUtils
            .parseBooleans(baseDisplayConfig.getAlwaysOnTop(), displayConfig.getAlwaysOnTop(),
                DEFAULT_ALWAYS_ON_TOP);

        // Sub objects
        DisplayCapture capture = DisplayCapture
            .build(displayInfo, baseDisplayConfig.getCapture(), displayConfig.getCapture(),
                ExpressionUtils.getExpressionMap(displayInfo));
        DisplayBorder border = DisplayBorder
            .build(baseDisplayConfig.getBorder(), displayConfig.getBorder());
        DisplayBackground background = DisplayBackground
            .build(displayInfo, baseDisplayConfig.getBackground(),
                displayConfig.getBackground(),
                ExpressionUtils.getExpressionMap(displayInfo, capture));
        DisplayText text = DisplayText.build(displayInfo,
            baseDisplayConfig.getText(), displayConfig.getText(),
            ExpressionUtils.getExpressionMap(displayInfo, capture, background));

        DisplayUpdate displayUpdate = DisplayUpdate.builder()
            .screen(screen)
            .alwaysOnTop(alwaysOnTop)
            .capture(capture)
            .background(background)
            .border(border)
            .text(text)
            .build();

        return displayUpdate;
    }

}
