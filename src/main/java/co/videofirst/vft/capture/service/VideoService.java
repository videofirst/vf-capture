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

import co.videofirst.vft.capture.model.params.VideoFinishParams;
import co.videofirst.vft.capture.model.params.VideoStartParams;
import co.videofirst.vft.capture.model.video.Video;
import co.videofirst.vft.capture.model.video.VideoStatus;
import co.videofirst.vft.capture.model.video.VideoSummary;
import java.util.List;

/**
 * High level Video service.
 *
 * @author Bob Marks
 */
public interface VideoService {

    /**
     * Select a video using a video id.
     */
    Video select(String videoId);

    /**
     * Return a list of video summaries.
     */
    List<VideoSummary> list();

    /**
     * Return the current status i.e. is it idle or in progress.
     */
    VideoStatus status();

    /**
     * Start the capture process (but not necessarily the actual screen recording).
     */
    VideoStatus start(VideoStartParams videoStartParams);

    /**
     * Start recording the screen (no parameters required).
     */
    VideoStatus record();

    /**
     * Stop recordinng the screen (no parameters required).
     */
    VideoStatus stop();

    /**
     * Finish capturing and finish video / test.
     */
    VideoStatus finish(VideoFinishParams videoFinishParams);

    /**
     * Cancel any current captures.
     */
    VideoStatus cancel();

    /**
     * Delete an existing test capture.
     */
    void delete(String videoId);

}
