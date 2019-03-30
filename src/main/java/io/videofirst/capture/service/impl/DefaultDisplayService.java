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
package io.videofirst.capture.service.impl;

import io.videofirst.capture.model.capture.CaptureStatus;
import io.videofirst.capture.model.display.DisplayBackground;
import io.videofirst.capture.model.display.DisplayBorder;
import io.videofirst.capture.model.display.DisplayCapture;
import io.videofirst.capture.model.display.DisplayText;
import io.videofirst.capture.model.display.DisplayUpdate;
import io.videofirst.capture.service.CaptureService;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the DisplayService interface.
 *
 * Multi-screen functionality. https://stackoverflow.com/questions/4627553/show-jframe-in-a-specific-screen-in-dual-monitor-configuration
 *
 * TODO - maybe refactor into to e.g. DisplayService (high level) / DisplayComponent (low level).
 *
 * @author Bob Marks
 */
@Component
public class DefaultDisplayService extends JWindow implements Observer {

    // Injected fields
    private final CaptureService captureService;

    // Stateful field
    private CaptureStatus captureStatus;
    private DisplayUpdate displayUpdate;

    @Autowired
    public DefaultDisplayService(CaptureService captureService) {
        this.captureService = captureService;
        this.captureService.addObserver(this);

        setBounds(getGraphicsConfiguration().getBounds());
        setBackground(new Color(0, true));
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (displayUpdate == null || displayUpdate.noDisplayUpdates()) {
            return;
        }

        setAlwaysOnTop(displayUpdate.isAlwaysOnTop());

        drawBackground(g);
        drawBorder(g);
        drawText(g);
    }

    // Private methods

    private void drawBackground(Graphics g) {
        DisplayBackground db = displayUpdate.getBackground();
        if (db != null && db.isDisplay()) {
            g.setColor(db.getColor());
            int x = db.getX(), y = db.getY(), w = db.getWidth() - 1, h = db.getHeight() - 1;
            g.fillRect(x, y, w, h);
        }
    }

    private void drawBorder(Graphics g) {
        DisplayBorder db = displayUpdate.getBorder();
        DisplayCapture dc = displayUpdate.getCapture();
        if (db != null && db.isDisplay()) {
            g.setColor(db.getColor());
            int pad = db.getPadding();
            for (int i = 0; i < db.getWidth(); i++) { // draw rectangle for each pixel of width
                int x = dc.getX() - pad;
                int y = dc.getY() - pad;
                int w = dc.getWidth() - 1 + (pad * 2);
                int h = dc.getHeight() - 1 + (pad * 2);
                g.drawRect(x, y, w, h);
                pad++;
            }
        }
    }

    private void drawText(Graphics g) {
        if (captureStatus == null) {
            return;
        }
        DisplayText displayText = displayUpdate.getText();
        final Font font = getFont().deriveFont(displayText.getFontSize());
        g.setFont(font);

        g.setColor(displayText.getColor());
        final String message =
            captureStatus != null ? ("Status : " + captureStatus.getState().toString())
                : ""; // FIXME improve
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(message, displayText.getX(), displayText.getY() + metrics.getHeight());
    }

    @Override
    public void update(Observable observable, Object arg) {
        // Set fields, then repaint
        this.displayUpdate = captureService.getDisplayUpdate();
        this.captureStatus = captureService.status();

        repaint();
    }

}
