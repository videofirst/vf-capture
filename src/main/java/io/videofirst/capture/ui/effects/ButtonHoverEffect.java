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
package io.videofirst.capture.ui.effects;

import static io.videofirst.capture.ui.constants.UiConstants.COLOR_WHITE;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Button hover effect. Sets the text of the button when it's hovered to give a better UX.
 *
 * @author Bob Marks
 */
public class ButtonHoverEffect extends MouseAdapter {

    private JButton component;
    private Color initialColor;

    public ButtonHoverEffect(JButton component) {
        this.component = component;
        this.initialColor = component.getForeground();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (this.component.isEnabled()) {
            this.component.setForeground(COLOR_WHITE);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e.getComponent().setForeground(initialColor);
    }

}
