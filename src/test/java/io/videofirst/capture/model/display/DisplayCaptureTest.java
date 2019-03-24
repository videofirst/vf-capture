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
package io.videofirst.capture.model.display;

import static org.assertj.core.api.Assertions.assertThat;

import io.videofirst.capture.configuration.properties.DisplayCaptureConfig;
import io.videofirst.capture.exception.VideoConfigurationException;
import io.videofirst.capture.model.info.DisplayInfo;
import io.videofirst.capture.utils.ExpressionUtils;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Unit test to test the methods of DisplayBackground.
 *
 * @author Bob Marks
 */
public class DisplayCaptureTest {

    @Test
    public void shouldUseDisplayInfoWhenBothBaseAndVideoCaptureConfigAreNull() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCaptureConfig baseCaptureConfig = null;
        DisplayCaptureConfig videoCaptureConfig = null;
        Map<String, Integer> expMap = null;

        DisplayCapture capture = DisplayCapture
            .build(displayInfo, baseCaptureConfig, videoCaptureConfig, expMap);

        assertThat(capture.getX()).isEqualTo(10);
        assertThat(capture.getY()).isEqualTo(20);
        assertThat(capture.getWidth()).isEqualTo(640);
        assertThat(capture.getHeight()).isEqualTo(480);
    }

    @Test
    public void shouldUseDisplayInfoWhenBothBaseAndVideoCaptureConfigFieldsAreNull() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCaptureConfig baseCaptureConfig = DisplayCaptureConfig.builder().build();
        DisplayCaptureConfig videoCaptureConfig = DisplayCaptureConfig.builder().build();
        Map<String, Integer> expMap = new HashMap<>();

        DisplayCapture capture = DisplayCapture
            .build(displayInfo, baseCaptureConfig, videoCaptureConfig, expMap);

        assertThat(capture.getX()).isEqualTo(10);
        assertThat(capture.getY()).isEqualTo(20);
        assertThat(capture.getWidth()).isEqualTo(640);
        assertThat(capture.getHeight()).isEqualTo(480);
    }

    @Test
    public void shouldUseDisplayInfoWhenBothBaseAndVideoCaptureConfigFieldsAreEmpty() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCaptureConfig baseCaptureConfig = DisplayCaptureConfig.builder()
            .x("").y("").width("").height("").build();
        DisplayCaptureConfig videoCaptureConfig = DisplayCaptureConfig.builder()
            .x("").y("").width("").height("").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo);

        DisplayCapture capture = DisplayCapture
            .build(displayInfo, baseCaptureConfig, videoCaptureConfig, expMap);

        assertThat(capture.getX()).isEqualTo(10);
        assertThat(capture.getY()).isEqualTo(20);
        assertThat(capture.getWidth()).isEqualTo(640);
        assertThat(capture.getHeight()).isEqualTo(480);
    }

    @Test
    public void shouldUseBaseCaptureWhenSet() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCaptureConfig baseCaptureConfig = DisplayCaptureConfig.builder()
            .x("30").y("40").width("650").height("490").build();
        DisplayCaptureConfig videoCaptureConfig = null;
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo);

        DisplayCapture capture = DisplayCapture
            .build(displayInfo, baseCaptureConfig, videoCaptureConfig, expMap);

        assertThat(capture.getX()).isEqualTo(30);
        assertThat(capture.getY()).isEqualTo(40);
        assertThat(capture.getWidth()).isEqualTo(650);
        assertThat(capture.getHeight()).isEqualTo(490);
    }

    @Test
    public void shouldUseVideoCaptureWhenSet() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCaptureConfig baseCaptureConfig = DisplayCaptureConfig.builder()
            .x("30").y("40").width("650").height("490").build();
        DisplayCaptureConfig videoCaptureConfig = DisplayCaptureConfig.builder()
            .x("50").y("60").width("660").height("500").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo);

        DisplayCapture capture = DisplayCapture
            .build(displayInfo, baseCaptureConfig, videoCaptureConfig, expMap);

        assertThat(capture.getX()).isEqualTo(50);
        assertThat(capture.getY()).isEqualTo(60);
        assertThat(capture.getWidth()).isEqualTo(660);
        assertThat(capture.getHeight()).isEqualTo(500);
    }

    @Test
    public void shouldUseBaseCaptureWhenSetWithSimpleCalcuations() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCaptureConfig baseCaptureConfig = DisplayCaptureConfig.builder()
            .x("30 / 2").y("(40 * 2) + 10").width("displayWidth - 10")
            .height("(displayHeight + 20) / 2").build();
        DisplayCaptureConfig videoCaptureConfig = null;
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo);

        DisplayCapture capture = DisplayCapture
            .build(displayInfo, baseCaptureConfig, videoCaptureConfig, expMap);

        assertThat(capture.getX()).isEqualTo(15);
        assertThat(capture.getY()).isEqualTo(90);
        assertThat(capture.getWidth()).isEqualTo(630);
        assertThat(capture.getHeight()).isEqualTo(250);
    }

    @Test
    public void shouldUseVideoCaptureWhenSetWithCalcuations() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(400).build();
        DisplayCaptureConfig baseCaptureConfig = DisplayCaptureConfig.builder()
            .x("30 / 2").y("(40 * 2) + 10").width("displayWidth - 10")
            .height("(displayWidth + 20) / 2").build();
        DisplayCaptureConfig videoCaptureConfig = DisplayCaptureConfig.builder()
            .x("20 / 2").y("(80 * 2) + 10").width("displayWidth - 100")
            .height("(displayHeight + 200) / 4").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo);

        DisplayCapture capture = DisplayCapture
            .build(displayInfo, baseCaptureConfig, videoCaptureConfig, expMap);

        assertThat(capture.getX()).isEqualTo(10);
        assertThat(capture.getY()).isEqualTo(170);
        assertThat(capture.getWidth()).isEqualTo(540);
        assertThat(capture.getHeight()).isEqualTo(150);
    }

    @Test
    public void shouldUseBothBaseAndVideoCaptureWithPartialOverrides() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(400).build();
        DisplayCaptureConfig baseCaptureConfig = DisplayCaptureConfig.builder()
            .x("70 / 2").y("").width("displayWidth - 60").height(null).build();
        DisplayCaptureConfig videoCaptureConfig = DisplayCaptureConfig.builder()
            .x(null).y("(10 * 2) + 10").width("").height("displayHeight + 100").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo);

        DisplayCapture capture = DisplayCapture
            .build(displayInfo, baseCaptureConfig, videoCaptureConfig, expMap);

        assertThat(capture.getX()).isEqualTo(35);
        assertThat(capture.getY()).isEqualTo(30);
        assertThat(capture.getWidth()).isEqualTo(580);
        assertThat(capture.getHeight()).isEqualTo(500);
    }

    @Test(expected = VideoConfigurationException.class)
    public void shouldNotAllowNegativeWidthOnBaseConfig() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(400).build();
        DisplayCaptureConfig baseCaptureConfig = DisplayCaptureConfig.builder()
            .x("70 / 2").y("").width(" -60").height(null).build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo);

        DisplayCapture.build(displayInfo, baseCaptureConfig, null, expMap);
    }

    @Test(expected = VideoConfigurationException.class)
    public void shouldNotAllowNegativeHeightOnVideoConfig() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(400).build();
        DisplayCaptureConfig videoCaptureConfig = DisplayCaptureConfig.builder()
            .x(null).y("(10 * 2) + 10").width("").height("-1").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo);

        DisplayCapture.build(displayInfo, null, videoCaptureConfig, expMap);
    }

}
