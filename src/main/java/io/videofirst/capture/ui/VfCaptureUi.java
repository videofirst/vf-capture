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
package io.videofirst.capture.ui;

import com.bulenkov.darcula.DarculaLaf;
import io.videofirst.capture.configuration.properties.CaptureConfig;
import io.videofirst.capture.exception.CaptureException;
import io.videofirst.capture.service.CaptureService;
import io.videofirst.capture.ui.components.VfCaptureFrame;
import javax.annotation.PostConstruct;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.basic.BasicLookAndFeel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Main class for the VF-Capture UI (user interface).
 *
 * @author Bob Marks
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class VfCaptureUi {

    private final CaptureConfig captureConfig;
    private final CaptureService captureService;

    @PostConstruct
    public void init() {
        if (captureConfig.getUi().isDisplay()) {
            createUi();
        }
    }

    // Private methods

    private void createUi() {
        setUiLookAndFeel();

        new VfCaptureFrame(captureService);
    }

    /**
     * Set look and feel (needs to be done before application loads up).
     */
    private void setUiLookAndFeel() {
        try {
            BasicLookAndFeel darcula = new DarculaLaf();
            UIManager.setLookAndFeel(darcula);
        } catch (UnsupportedLookAndFeelException ex) {
            throw new CaptureException("Look and feel not supported");
        }
    }

}
