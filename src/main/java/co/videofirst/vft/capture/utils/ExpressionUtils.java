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
package co.videofirst.vft.capture.utils;

import co.videofirst.vft.capture.model.display.DisplayBackground;
import co.videofirst.vft.capture.model.display.DisplayCapture;
import co.videofirst.vft.capture.model.info.DisplayInfo;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for creating an expression map from objects.
 *
 * TODO - probably more elegant way of create expression maps - maybe move to each object?
 *
 * @author Bob Marks
 */
public class ExpressionUtils {

    public static Map<String, Integer> getExpressionMap(DisplayInfo displayInfo) {
        if (displayInfo == null) {
            return new HashMap<>();
        }
        Map<String, Integer> expressionMap = ImmutableMap.of(
            "displayX", displayInfo.getX(),
            "displayY", displayInfo.getY(),
            "displayWidth", displayInfo.getWidth(),
            "displayHeight", displayInfo.getHeight());
        return expressionMap;
    }

    public static Map<String, Integer> getExpressionMap(DisplayInfo displayInfo,
        DisplayCapture displayCapture) {
        if (displayCapture == null) {
            return new HashMap<>();
        }
        final Map<String, Integer> expressionMap = new ImmutableMap.Builder()
            .putAll(new HashMap<String, Integer>() {{
                putAll(getExpressionMap(displayInfo));
                put("captureX", displayCapture.getX());
                put("captureY", displayCapture.getY());
                put("captureWidth", displayCapture.getWidth());
                put("captureHeight", displayCapture.getHeight());
            }})
            .build();
        return expressionMap;
    }

    public static Map<String, Integer> getExpressionMap(DisplayInfo displayInfo,
        DisplayCapture displayCapture, DisplayBackground displayBackground) {
        if (displayBackground == null) {
            return new HashMap<>();
        }
        final Map<String, Integer> expressionMap = new ImmutableMap.Builder()
            .putAll(new HashMap<String, Integer>() {{
                putAll(getExpressionMap(displayInfo, displayCapture));
                put("backgroundX", displayBackground.getX());
                put("backgroundY", displayBackground.getY());
                put("backgroundWidth", displayBackground.getWidth());
                put("backgroundHeight", displayBackground.getHeight());
            }})
            .build();
        return expressionMap;
    }

}
