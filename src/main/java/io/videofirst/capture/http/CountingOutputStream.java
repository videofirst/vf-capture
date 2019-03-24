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
package io.videofirst.capture.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * See [ http://www.baeldung.com/httpclient-post-http-request - 8. Get File Upload Progress ] for
 * original.
 *
 * Modified slightly so that the listener isn't being called with every update.
 */
public class CountingOutputStream extends FilterOutputStream {

    private final static int LISTENER_UPDATE_THRESHOLD = 4000; // update every 4,000 bytes

    // Injected fields

    private final ProgressListener listener;
    private final long totalBytes;

    // Stateful fields

    private long transferred;
    private long listenerUpdate;

    public CountingOutputStream(
        OutputStream out, ProgressListener listener, long totalBytes) {
        super(out);
        this.listener = listener;
        this.totalBytes = totalBytes;

        transferred = 0;
        listenerUpdate = 0;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        updateProgress(len);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        updateProgress(1);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    // Private methods

    private void updateProgress(int len) {
        transferred += len;
        listenerUpdate += len;
        if (listenerUpdate > LISTENER_UPDATE_THRESHOLD || transferred == totalBytes) {
            listener.progress(transferred, totalBytes);
            listenerUpdate = 0; // reset
        }
    }
}
