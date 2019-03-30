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
package io.videofirst.capture.ui.components;

import static io.videofirst.capture.ui.constants.UiConstants.CAPTURE_ICON_16;

import io.videofirst.capture.service.CaptureService;
import io.videofirst.capture.ui.panels.RecordPanel;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Main UI Frame.
 *
 * @author Bob Marks
 */
public class VfCaptureFrame extends JFrame {

    private static final int width = 800;  // Put in properties ??? sticky properties
    private static final int height = 800;

    private final CaptureService captureService;

    private TrayIcon trayIcon;
    private SystemTray tray;

    public VfCaptureFrame(CaptureService captureService) {
        super("Video First Capture");

        this.captureService = captureService;

        init();

        setupSystemTray();
    }

    // Private methods

    private void init() {
        this.getContentPane().add(new RecordPanel(captureService), BorderLayout.CENTER);

        // Set size / position
        Dimension size = new Dimension(width, height);
        Point location = getCentredLocation(this, size);

        this.setSize(size);
        this.setLocation(location.x, location.y);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(CAPTURE_ICON_16));
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Private methods

    private void setupSystemTray() {
        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();

            Image image = Toolkit.getDefaultToolkit().getImage(CAPTURE_ICON_16);
            ActionListener exitListener = e -> System.exit(0);

            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
            defaultItem = new MenuItem("Open");
            defaultItem.addActionListener(e -> {
                showCaptureFrame();
            });
            popup.add(defaultItem);
            trayIcon = new TrayIcon(image, "Video First Capture", popup);
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        showCaptureFrame();
                    }
                }

            });

            addWindowStateListener(e -> {
                if (e.getNewState() == ICONIFIED || e.getNewState() == 7) {
                    trayHide(true);
                }
                if (e.getNewState() == MAXIMIZED_BOTH || e.getNewState() == NORMAL) {
                    trayHide(false);
                }
            });
        } else {
            System.out.println("system tray not supported");
        }
    }

    private void showCaptureFrame() {
        setVisible(true);
        setExtendedState(JFrame.NORMAL);
    }

    private void trayHide(boolean isHide) {
        try {
            if (isHide) {
                tray.add(trayIcon);
                setVisible(false);
            } else { // show again
                tray.remove(trayIcon);
                setVisible(true);
            }
        } catch (AWTException ex) {
            System.err.println("Couldn't show/hide tray");
        }
    }

    private Point getCentredLocation(Window window, Dimension componentSize) {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gs.length; i++) {
            DisplayMode dm = gs[i].getDisplayMode();
            sb.append(i + ", width: " + dm.getWidth() + ", height: " + dm.getHeight() + "\n");
        }
        System.out.println(sb.toString());

        //int currentScreenWidth = gs[0].getDisplayMode().getWidth();
        //        //int currentScreenHeight = gs[0].getDisplayMode().getWidth();

        Dimension screenSize = window.getToolkit().getScreenSize();
        Point p = new Point(
            (int) (screenSize.getWidth() / 2 - componentSize.getWidth() / 2),
            (int) (screenSize.getHeight() / 2 - componentSize.getHeight() / 2));

        return p;
    }

}