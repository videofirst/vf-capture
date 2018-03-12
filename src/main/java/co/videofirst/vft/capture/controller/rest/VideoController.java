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

import co.videofirst.vft.capture.model.params.VideoFinishParams;
import co.videofirst.vft.capture.model.params.VideoStartParams;
import co.videofirst.vft.capture.model.video.UploadStatus;
import co.videofirst.vft.capture.model.video.Video;
import co.videofirst.vft.capture.model.video.VideoStatus;
import co.videofirst.vft.capture.model.video.VideoSummary;
import co.videofirst.vft.capture.service.UploadService;
import co.videofirst.vft.capture.service.VideoService;
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
 * Main controller which is used to capture video and upload when finished.
 *
 * @author Bob Marks
 */
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final UploadService uploadService;

    @GetMapping
    public List<VideoSummary> list() {
        List<VideoSummary> list = videoService.list();
        return list;
    }

    @GetMapping("/{videoId}")
    public Video select(@PathVariable final String videoId) {
        Video video = videoService.select(videoId);
        return video;
    }

    @PostMapping("/start")
    public VideoStatus start(@RequestBody VideoStartParams param) {
        VideoStatus status = videoService.start(param);
        return status;
    }

    @PostMapping("/record")
    public VideoStatus record() {
        VideoStatus status = videoService.record();
        return status;
    }

    @PostMapping("/stop")
    public VideoStatus stop() {
        VideoStatus status = videoService.stop();
        return status;
    }

    @PostMapping("finish")
    public VideoStatus finish(@RequestBody VideoFinishParams videoFinishParams) {
        VideoStatus status = videoService.finish(videoFinishParams);
        return status;
    }

    @PostMapping("cancel")
    public VideoStatus cancel() {
        VideoStatus status = videoService.cancel();
        return status;
    }

    @GetMapping("/status")
    public VideoStatus status() {
        VideoStatus status = videoService.status();
        return status;
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> delete(@PathVariable final String videoId) {
        videoService.delete(videoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload/{videoId}")
    public List<UploadStatus> uploadByVideoId(@PathVariable final String videoId) {
        uploadService.upload(videoId);
        return uploadStatus();
    }

    @GetMapping("/upload")
    public List<UploadStatus> uploadStatus() {
        return uploadService.status();
    }

}
