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
package co.videofirst.vft.capture.controller.rest;

import co.videofirst.vft.capture.model.params.CaptureFinishParams;
import co.videofirst.vft.capture.model.params.CaptureStartParams;
import co.videofirst.vft.capture.model.capture.Capture;
import co.videofirst.vft.capture.model.capture.UploadStatus;
import co.videofirst.vft.capture.model.capture.CaptureStatus;
import co.videofirst.vft.capture.model.capture.CaptureSummary;
import co.videofirst.vft.capture.service.UploadService;
import co.videofirst.vft.capture.service.CaptureService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main controller which is used to capture video / test data, then  upload when finished.
 *
 * @author Bob Marks
 */
@RestController
@RequestMapping("/api/captures")
@RequiredArgsConstructor
public class CaptureController {

    private final CaptureService captureService;
    private final UploadService uploadService;

    @GetMapping
    public List<CaptureSummary> list() {
        List<CaptureSummary> list = captureService.list();
        return list;
    }

    @GetMapping("/{captureId}")
    public Capture select(@PathVariable final String captureId) {
        Capture capture = captureService.select(captureId);
        return capture;
    }

    @PostMapping("/start")
    public CaptureStatus start(@RequestBody CaptureStartParams param) {
        CaptureStatus status = captureService.start(param);
        return status;
    }

    @PostMapping("/record")
    public CaptureStatus record() {
        CaptureStatus status = captureService.record();
        return status;
    }

    @PostMapping("/stop")
    public CaptureStatus stop() {
        CaptureStatus status = captureService.stop();
        return status;
    }

    @PostMapping("finish")
    public CaptureStatus finish(@RequestBody CaptureFinishParams captureFinishParams) {
        CaptureStatus status = captureService.finish(captureFinishParams);
        return status;
    }

    @PostMapping("cancel")
    public CaptureStatus cancel() {
        CaptureStatus status = captureService.cancel();
        return status;
    }

    @GetMapping("/status")
    public CaptureStatus status() {
        CaptureStatus status = captureService.status();
        return status;
    }

    @DeleteMapping("/{captureId}")
    public ResponseEntity<Void> delete(@PathVariable final String captureId) {
        captureService.delete(captureId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload/{captureId}")
    public List<UploadStatus> uploadByCaptureId(@PathVariable final String captureId) {
        uploadService.upload(captureId);
        return uploadStatus();
    }

    @GetMapping("/upload")
    public List<UploadStatus> uploadStatus() {
        return uploadService.status();
    }

}
