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
package io.videofirst.capture.ui.constants;

import info.clearthought.layout.TableLayout;
import java.awt.Color;
import java.net.URL;

/**
 * UI constants.
 *
 * @author Bob Marks
 */
public interface UiConstants {

    double P = TableLayout.PREFERRED;
    double F = TableLayout.FILL;
    double M_1 = 10;

    // Icons

    URL CAPTURE_ICON_16 = UiConstants.class.getResource("/icons/icon-capture-16.gif");

    // Colors

    Color COLOR_PRIMARY = new Color(0, 150, 250);
    Color COLOR_LIGHT_GREY = new Color(245, 245, 245);
    Color COLOR_WHITE = new Color(255, 255, 255);

}
