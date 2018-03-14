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
package co.videofirst.vft.capture.recorder.monte;

import static co.videofirst.vft.capture.test.VftTesting.VFT_TEMP_FOLDER;
import static co.videofirst.vft.capture.test.VftTesting.VFT_VIDEO_FOLDER;

import co.videofirst.vft.capture.model.display.DisplayCapture;
import co.videofirst.vft.capture.recorder.VideoRecord;
import co.videofirst.vft.capture.test.VftTesting;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test to test the methods of VideoRecorderMonte.
 *
 * @author Bob Marks
 */
public class VideoRecorderMonteTest {

    private final VideoRecorderMonte monte = new VideoRecorderMonte(VFT_TEMP_FOLDER,
        VFT_VIDEO_FOLDER);

    @Before
    public void setUp() throws IOException {
        VftTesting.initTestFolders();
    }

    @After
    public void tearDown() throws IOException {
        VftTesting.cleanTestFolders();
    }

    @Test
    public void shouldRecord() throws InterruptedException {

        VideoRecord record = VideoRecord.builder()
            .id("monte-id-123")
            .folder("monte-test-folder")
            .format("avi")
            .capture(DisplayCapture.builder().x(0).y(0).width(1920).height(1200).build())
            .build();

        monte.record(record);

        Thread.sleep(1000);
        
        monte.stop();
    }

}
