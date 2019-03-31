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

import io.videofirst.capture.dao.CaptureDao;
import io.videofirst.capture.exception.InvalidParameterException;
import io.videofirst.capture.model.capture.Capture;
import io.videofirst.capture.model.capture.CaptureRecordParams;
import io.videofirst.capture.model.capture.CaptureStatus;
import io.videofirst.capture.model.capture.CaptureStopParams;
import io.videofirst.capture.model.capture.CaptureSummary;
import io.videofirst.capture.model.display.DisplayUpdate;
import io.videofirst.capture.recorder.VideoRecord;
import io.videofirst.capture.recorder.VideoRecorder;
import io.videofirst.capture.service.CaptureService;
import io.videofirst.capture.service.InfoService;
import java.util.List;
import java.util.Observable;
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
public class DefaultCaptureService extends Observable implements CaptureService {

    // Injected fields

    private final VideoRecorder videoRecorder;
    private final InfoService infoService;
    private final CaptureDao captureDao;

    // Local fields

    private CaptureStatus captureStatus = CaptureStatus.STOPPED;  // only stateful object

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
    public CaptureStatus record(CaptureRecordParams captureRecordParams) {
        if (captureRecordParams.force()) {
            cancelCapture();
        }

        DisplayUpdate displayUpdate = getDisplayUpdate(); // move to
        captureStatus = captureStatus
            .record(infoService.getInfo(), captureRecordParams, displayUpdate.getCapture());

        videoRecorder.record(getVideoRecord());

        refreshObservers();
        return status();
    }

    @Override
    public CaptureStatus stop(CaptureStopParams captureStopParams) {
        if (!captureStatus.isRecording()) {
            throw new InvalidParameterException(
                "You can only stop a video when [ isRecording=true ]");
        }

        captureStatus = captureStatus.stop(captureStopParams);

        videoRecorder.stop();

        captureDao.save(captureStatus.getCapture());

        refreshObservers();
        return status();
    }

    @Override
    public CaptureStatus cancel() {
        cancelCapture();

        refreshObservers();
        return status();
    }

    @Override
    public void delete(String captureId) {
        captureDao.delete(captureId);

        refreshObservers();
    }

    /**
     * Generate display update.
     */
    @Override
    public DisplayUpdate getDisplayUpdate() {
        return DisplayUpdate
            .build(infoService.getInfo(), captureStatus.getCaptureRecordParams().getDisplay());
    }

    // Private methods

    private void cancelCapture() {
        videoRecorder.cancel();  // cancel any recording if applicable
        captureStatus = CaptureStatus.STOPPED; // re-set status
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

    private void refreshObservers() {
        setChanged();
        notifyObservers();    // notify any class which observe this class
    }

}
