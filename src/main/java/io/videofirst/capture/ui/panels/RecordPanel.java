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
package io.videofirst.capture.ui.panels;

import static io.videofirst.capture.ui.constants.UiConstants.F;
import static io.videofirst.capture.ui.constants.UiConstants.M_1;
import static io.videofirst.capture.ui.constants.UiConstants.P;

import io.videofirst.capture.model.capture.CaptureRecordParams;
import io.videofirst.capture.model.capture.CaptureStatus;
import io.videofirst.capture.model.capture.CaptureStopParams;
import io.videofirst.capture.service.CaptureService;
import io.videofirst.capture.ui.components.VfButton;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Record panel.
 *
 * @author Bob Marks
 */
public class RecordPanel extends VfPanel implements Observer, MouseListener {

    // Constants

    private static final double[][] SIZES = {
        {M_1, F, M_1}, {M_1, P, M_1, P, M_1}};

    // Injected fields

    private final CaptureService captureService;

    // Other fields

    private VfButton recordButton, stopButton;
    private CaptureRecordParams captureRecordParams = new CaptureRecordParams();
    private CaptureStopParams captureStopParams = new CaptureStopParams();

    public RecordPanel(CaptureService captureService) {
        super(SIZES);

        this.captureService = captureService;
        this.captureService.addObserver(this);

        init();
    }

    @Override
    public void update(Observable o, Object arg) {
        refresh();
    }

    // Private methods

    private void init() {
        add(getButtonPanels(), "1,1");
        add(getSettingsPanel(), "1,3");

        refresh();
    }

    private JPanel getButtonPanels() {
        double[][] sizes = {{0.5, M_1, 0.5}, {P}};
        VfPanel panel = new VfPanel(sizes);

        panel.add(getRecordButton(), "0,0");
        panel.add(getStopButton(), "2,0");

        return panel;
    }

    private JPanel getSettingsPanel() {
        double[][] sizes = {{M_1, 0.5, M_1, 0.5, M_1}, {M_1, P, M_1}};
        VfPanel panel = new VfPanel(sizes, "Record Settings");

        return panel;
    }

    // Buttons

    private JButton getRecordButton() {
        recordButton = new VfButton("Record");
        recordButton.addMouseListener(this);

        return recordButton;
    }

    private JButton getStopButton() {
        stopButton = new VfButton("Stop");
        stopButton.addMouseListener(this);
        return stopButton;
    }

    /**
     * Refresh method.
     */
    private void refresh() {
        CaptureStatus captureStatus = this.captureService.status();
        //DisplayUpdate displayUpdate = this.captureService.getDisplayUpdate();

        recordButton.setEnabled(!captureStatus.isRecording());
        stopButton.setEnabled(captureStatus.isRecording());
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getSource() instanceof JButton) {
            JButton button = (JButton) event.getSource();
            if (button.isEnabled()) {
                String name = button.getName();
                if ("Record".equals(name)) {
                    this.captureService.record(captureRecordParams);
                } else if ("Stop".equals(name)) {
                    this.captureService.stop(captureStopParams);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

}
