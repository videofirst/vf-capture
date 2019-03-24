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

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.FormatKeys.MediaType;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.FormatKeys.MimeTypeKey;
import static org.monte.media.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.VideoFormatKeys.QualityKey;

import co.videofirst.vft.capture.exception.VideoRecordException;
import co.videofirst.vft.capture.model.display.DisplayCapture;
import co.videofirst.vft.capture.recorder.VideoRecord;
import co.videofirst.vft.capture.recorder.VideoRecorder;
import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Monte implementation of the VideoRecorder interface.
 *
 * @author Bob Marks
 */
@Slf4j
@Component
public class VideoRecorderMonte implements VideoRecorder {

    private static final int DEFAULT_FRAME_RATE = 10;

    // Fields created by constructor

    private final GraphicsConfiguration graphicsConfiguration;
    private final Format fileFormat;
    private final Format screenFormat;
    private final Format mouseFormat;
    private final Format audioFormat;
    private final File tempFolder;
    private final File videoFolder;
    private final int frameRate = DEFAULT_FRAME_RATE; // put in configuration in future

    // Other fields

    private EnhancedScreenRecorder screenRecorder;
    private VideoRecord videoRecord;

    /**
     * Constructor which takes temporary video folder (while videos are being recorded) and final
     * output folder.
     */
    @Autowired
    public VideoRecorderMonte(@Value("${vft_config.storage.videoFolder}") File tempFolder,
        @Value("${vft_config.storage.videoFolder}") File videoFolder) {
        this.tempFolder = tempFolder;
        this.videoFolder = videoFolder;

        graphicsConfiguration = getGraphicsConfiguration();
        fileFormat = getFileFormat();
        screenFormat = getScreenFormat(frameRate);
        mouseFormat = getMouseFormat(frameRate);
        audioFormat = getAudioFormat();
    }

    @Override
    public void record(VideoRecord videoRecord) {
        try {
            this.videoRecord = videoRecord;
            DisplayCapture displayCapture = videoRecord.getCapture();
            Rectangle captureArea = new Rectangle(displayCapture.getX(), displayCapture.getY(),
                displayCapture.getWidth(), displayCapture.getHeight());

            screenRecorder = new EnhancedScreenRecorder(graphicsConfiguration,
                captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, tempFolder);
            screenRecorder.start();
        } catch (IOException ioEx) {
            throw new VideoRecordException("Record exception when starting recording", ioEx);
        } catch (AWTException awtEx) {
            throw new VideoRecordException("Record exception when starting recording", awtEx);
        }
    }

    @Override
    public void stop() {
        if (screenRecorder == null) {
            log.debug("The screenRecorder field is null - has the recording started yet?");
            return;
        }
        if (videoRecord == null) {
            log.debug("The videoRecord field is null - has the recording started yet?");
            return;
        }

        screenRecorder.stop();

        File videoDir = new File(videoFolder, videoRecord.getFolder());
        videoDir.mkdirs(); // ensure directory structure is created

        File tempFile = screenRecorder.getCreatedMovieFiles().get(0);
        File videoFile = new File(videoDir, videoRecord.getId() + "." + videoRecord.getFormat());
        tempFile.renameTo(videoFile);
    }

    @Override
    public void cancel() {
        if (screenRecorder == null) {
            log.debug("Screen-recorder is null");
            return;
        }

        screenRecorder.stop();

        // Delete file if it exists
        File tempFile = screenRecorder.getCreatedMovieFiles().get(0);
        if (tempFile != null) {
            tempFile.delete();
        }
    }

    // Private methods

    private GraphicsConfiguration getGraphicsConfiguration() {
        GraphicsConfiguration GraphicsConfiguration = GraphicsEnvironment
            .getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration();
        return GraphicsConfiguration;
    }

    private Format getFileFormat() {
        Format fileFormat = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, FormatKeys.MIME_AVI);
        return fileFormat;
    }

    private Format getScreenFormat(int frameRate) {
        Format screenFormat = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey,
            ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
            CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
            DepthKey, 24, FrameRateKey, Rational.valueOf(frameRate),
            QualityKey, 1.0f,
            KeyFrameIntervalKey, 15 * 60);
        return screenFormat;
    }

    private Format getMouseFormat(int frameRate) {
        Format mouseFormat = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
            FrameRateKey, Rational.valueOf(frameRate));
        return mouseFormat;
    }

    public Format getAudioFormat() {
        return null;
    }

    /**
     * Extend Monte ScreenRecorder as some fields / methods are protected and we need access to
     * them.
     */
    private class EnhancedScreenRecorder extends ScreenRecorder {

        public EnhancedScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea,
            Format fileFormat, Format screenFormat, Format mouseFormat,
            Format audioFormat, File movieFolder) throws IOException, AWTException {
            super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat,
                movieFolder);
        }

        @Override
        public File createMovieFile(Format fileFormat) {
            try {
                return super.createMovieFile(fileFormat);
            } catch (IOException e) {
                throw new VideoRecordException(e);
            }
        }

        @Override
        public void stop() {
            try {
                super.stop();
            } catch (IOException e) {
                throw new VideoRecordException(e);
            }
        }
    }

}
