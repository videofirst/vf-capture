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
package co.videofirst.vft.capture.service.impl;

import co.videofirst.vft.capture.dao.CaptureDao;
import co.videofirst.vft.capture.enums.CaptureState;
import co.videofirst.vft.capture.exception.InvalidParameterException;
import co.videofirst.vft.capture.exception.InvalidStateException;
import co.videofirst.vft.capture.model.capture.Capture;
import co.videofirst.vft.capture.model.capture.CaptureFinishParams;
import co.videofirst.vft.capture.model.capture.CaptureStartParams;
import co.videofirst.vft.capture.model.capture.CaptureStatus;
import co.videofirst.vft.capture.model.capture.CaptureSummary;
import co.videofirst.vft.capture.model.display.DisplayUpdate;
import co.videofirst.vft.capture.recorder.VideoRecord;
import co.videofirst.vft.capture.recorder.VideoRecorder;
import co.videofirst.vft.capture.service.CaptureService;
import co.videofirst.vft.capture.service.DisplayService;
import co.videofirst.vft.capture.service.InfoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the high level CaptureService interface.
 *
 * @author Bob Marks
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultCaptureService implements CaptureService {

    // Injected fields

    private final VideoRecorder videoRecorder;
    private final DisplayService displayService;
    private final InfoService infoService;
    private final CaptureDao captureDao;

    // Local fields

    private CaptureStatus captureStatus = CaptureStatus.IDLE;  // only stateful object

    @Override
    public Capture select(String captureId) {
        Capture capture = captureDao.findById(captureId);
        return capture;
    }

    @Override
    public List<CaptureSummary> list() {
        List<CaptureSummary> list = captureDao.list();
        return list;
    }

    @Override
    public CaptureStatus status() {
        return captureStatus;
    }

    @Override
    public CaptureStatus start(CaptureStartParams captureStartParams) {
        if (captureStartParams == null) {
            captureStartParams = CaptureStartParams.builder().build();
        }
        if (captureStartParams.force()) {
            cancelCapture();
        }

        validateStart(captureStartParams);

        captureStatus = CaptureStatus.start(infoService.getInfo(), captureStartParams);

        if (captureStartParams.record()) {
            record();
        }

        refreshDisplay();
        return status();
    }

    @Override
    public CaptureStatus record() {
        if (captureStatus.getState() != CaptureState.started) {
            throw new InvalidStateException(
                "Current state is '" + captureStatus.getState() + "' - " +
                    "video can only be recoded when state is  'started'.");
        }

        DisplayUpdate displayUpdate = getDisplayUpdate();

        captureStatus = captureStatus.record(displayUpdate.getCapture());
        videoRecorder.record(getVideoRecord());

        refreshDisplay();

        return status();
    }

    @Override
    public CaptureStatus stop() {
        if (captureStatus.getState() != CaptureState.recording) {
            throw new InvalidParameterException(
                "Current state is '" + captureStatus.getState() + "' - " +
                    "You can only stop a video when it is in a state is 'recording'.");
        }

        captureStatus = captureStatus.stop();
        videoRecorder.stop();
        refreshDisplay();

        return status();
    }

    @Override
    public CaptureStatus finish(CaptureFinishParams finishVideo) {

        validateFinish(finishVideo);

        if (captureStatus.getState() == CaptureState.recording) {
            stop();
        }

        CaptureStatus finishedCaptureStatus = captureStatus.finish(finishVideo);
        captureDao.save(finishedCaptureStatus.getCapture());

        captureStatus = CaptureStatus.IDLE; // mark current status as stopped ..
        refreshDisplay();

        return finishedCaptureStatus;  // ... although return the finished status to the user
    }

    @Override
    public CaptureStatus cancel() {
        cancelCapture();
        refreshDisplay();

        return status();
    }

    @Override
    public void delete(String captureId) {
        captureDao.delete(captureId);
    }

    // Private methods

    private void cancelCapture() {
        videoRecorder.cancel();  // cancel any recording if applicable
        captureStatus = CaptureStatus.IDLE; // re-set status
    }

    /**
     * Validate start parameters.
     */
    private void validateStart(CaptureStartParams captureStartParams) {

        if (captureStatus.getState() != CaptureState.idle
            && captureStatus.getState() != CaptureState.started) {
            throw new InvalidStateException(
                "Current state is '" + captureStatus.getState() + "' - " +
                    "video can only be started when state is 'idle', 'uploaded' or re-started when it is in a state is 'started'. "
                    + "Please finish recording or cancel.");
        }
    }

    private void validateFinish(CaptureFinishParams finishVideoParams) {
        // Check state first of all
        if (captureStatus.getState() != CaptureState.recording
            && captureStatus.getState() != CaptureState.stopped) {
            throw new InvalidStateException(
                "Current state is '" + captureStatus.getState() + "' - " +
                    "videos can only be finished which is state is `recording` OR `stopped`.");
        }

        if (finishVideoParams.getTestStatus() == null) {
            throw new InvalidParameterException("Please fill in missing 'testStatus' attribute");
        }
    }

    /**
     * Generate display update.
     */
    private DisplayUpdate getDisplayUpdate() {
        return DisplayUpdate
            .build(infoService.getInfo(), captureStatus.getCaptureStartParams().getDisplay());
    }

    /**
     * Return video record.
     */
    private VideoRecord getVideoRecord() {
        if (captureStatus == null || captureStatus.getCapture() == null) {
            log.warn("Capture record is null");
            return null;
        }

        Capture capture = captureStatus.getCapture();
        return VideoRecord.builder()
            .id(capture.getId())
            .folder(capture.getFolder())
            .format(capture.getFormat())
            .capture(capture.getCapture())
            .build();
    }

    /**
     * Refresh display.
     */
    private void refreshDisplay() {
        DisplayUpdate displayUpdate = getDisplayUpdate();
        displayService.update(displayUpdate, captureStatus);
    }

}
