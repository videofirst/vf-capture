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
package co.videofirst.vft.capture.service;

import co.videofirst.vft.capture.model.params.CaptureFinishParams;
import co.videofirst.vft.capture.model.params.CaptureStartParams;
import co.videofirst.vft.capture.model.capture.Capture;
import co.videofirst.vft.capture.model.capture.CaptureStatus;
import co.videofirst.vft.capture.model.capture.CaptureSummary;
import co.videofirst.vft.capture.model.capture.CaptureStatus;
import java.util.List;

/**
 * High level capture service.
 *
 * @author Bob Marks
 */
public interface CaptureService {

    /**
     * Select a video using a capture id.
     */
    Capture select(String captureId);

    /**
     * Return a list of capture summaries.
     */
    List<CaptureSummary> list();

    /**
     * Return the current status i.e. is it idle or in progress.
     */
    CaptureStatus status();

    /**
     * Start the capture process (but not necessarily the actual screen recording).
     */
    CaptureStatus start(CaptureStartParams captureStartParams);

    /**
     * Start recording the screen (no parameters required).
     */
    CaptureStatus record();

    /**
     * Stop recordinng the screen (no parameters required).
     */
    CaptureStatus stop();

    /**
     * Finish capturing and finish video / test.
     */
    CaptureStatus finish(CaptureFinishParams captureFinishParams);

    /**
     * Cancel any current captures.
     */
    CaptureStatus cancel();

    /**
     * Delete an existing test capture.
     */
    void delete(String captureId);

}
