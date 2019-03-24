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
package co.videofirst.vft.capture.test;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 * Collection of use test test constants / methods.
 *
 * @author Bob Marks
 */
public class VftTesting {

    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    public static final File VFT_CAPTURE_TEST_FOLDER = new File(TEMP_DIR, "vft-capture-test");
    public static final File VFT_TEMP_FOLDER = new File(VFT_CAPTURE_TEST_FOLDER, "temp");
    public static final File VFT_VIDEO_FOLDER = new File(VFT_CAPTURE_TEST_FOLDER, "videos");
    public static final File TEST_VIDEOS = new File("src/test/resources/videos");

    public static void initTestFolders() throws IOException {
        cleanTestFolders();

        // copy files from `src/test/resources/videos` to `vft-capture-test/videos` folder
        FileUtils.copyDirectory(TEST_VIDEOS, VFT_VIDEO_FOLDER);
    }

    public static void cleanTestFolders() throws IOException {
        VFT_TEMP_FOLDER.mkdirs();
        FileUtils.cleanDirectory(VFT_TEMP_FOLDER);

        VFT_VIDEO_FOLDER.mkdirs();
        FileUtils.cleanDirectory(VFT_VIDEO_FOLDER);
    }

}
