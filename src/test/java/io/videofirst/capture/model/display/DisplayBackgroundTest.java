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

import io.videofirst.capture.configuration.properties.DisplayBackgroundConfig;
import io.videofirst.capture.exception.VideoConfigurationException;
import io.videofirst.capture.model.info.DisplayInfo;
import io.videofirst.capture.utils.ExpressionUtils;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Unit test to test the methods of DisplayBackground.
 *
 * @author Bob Marks
 */
public class DisplayBackgroundTest {

    @Test
    public void shouldUseDisplayInfoWhenBothBaseAndVideoBackgroundConfigAreNull() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayBackgroundConfig baseBackgroundConfig = null;
        DisplayBackgroundConfig videoBackgroundConfig = null;
        Map<String, Integer> expMap = null;

        DisplayBackground background = DisplayBackground
            .build(displayInfo, baseBackgroundConfig, videoBackgroundConfig, expMap);

        assertThat(background.getX()).isEqualTo(10);
        assertThat(background.getY()).isEqualTo(20);
        assertThat(background.getWidth()).isEqualTo(640);
        assertThat(background.getHeight()).isEqualTo(480);
    }

    @Test
    public void shouldUseDisplayInfoWhenBothBaseAndVideoBackgroundConfigFieldsAreNull() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayBackgroundConfig baseBackgroundConfig = DisplayBackgroundConfig.builder().build();
        DisplayBackgroundConfig videoBackgroundConfig = DisplayBackgroundConfig.builder().build();
        Map<String, Integer> expMap = new HashMap<>();

        DisplayBackground background = DisplayBackground
            .build(displayInfo, baseBackgroundConfig, videoBackgroundConfig, expMap);

        assertThat(background.getX()).isEqualTo(10);
        assertThat(background.getY()).isEqualTo(20);
        assertThat(background.getWidth()).isEqualTo(640);
        assertThat(background.getHeight()).isEqualTo(480);
    }

    @Test
    public void shouldUseDisplayInfoWhenBothBaseAndVideBackgroundeConfigFieldsAreEmpty() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackgroundConfig baseBackgroundConfig = DisplayBackgroundConfig.builder()
            .display("").color("").x("").y("").width("").height("").build();
        DisplayBackgroundConfig videoBackgroundConfig = DisplayBackgroundConfig.builder()
            .display("").color("").x("").y("").width("").height("").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo, videoCapture);

        DisplayBackground background = DisplayBackground
            .build(displayInfo, baseBackgroundConfig, videoBackgroundConfig, expMap);

        assertThat(background.isDisplay()).isEqualTo(DisplayBackground.DEFAULT_BACKGROUND_DISPLAY);
        assertThat(background.getColor()).isEqualTo(DisplayBackground.DEFAULT_BACKGROUND_COLOR);
        assertThat(background.getX()).isEqualTo(10);
        assertThat(background.getY()).isEqualTo(20);
        assertThat(background.getWidth()).isEqualTo(640);
        assertThat(background.getHeight()).isEqualTo(480);
    }

    @Test
    public void shouldUseBaseCaptureWhenSet() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackgroundConfig baseBackgroundConfig = DisplayBackgroundConfig.builder()
            .x("30").y("40").width("650").height("490").build();
        DisplayBackgroundConfig videoBackgroundConfig = null;
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo, videoCapture);

        DisplayBackground background = DisplayBackground
            .build(displayInfo, baseBackgroundConfig, videoBackgroundConfig, expMap);

        assertThat(background.isDisplay()).isEqualTo(DisplayBackground.DEFAULT_BACKGROUND_DISPLAY);
        assertThat(background.getColor()).isEqualTo(DisplayBackground.DEFAULT_BACKGROUND_COLOR);
        assertThat(background.getX()).isEqualTo(30);
        assertThat(background.getY()).isEqualTo(40);
        assertThat(background.getWidth()).isEqualTo(650);
        assertThat(background.getHeight()).isEqualTo(490);
    }

    @Test
    public void shouldUseVideoCaptureWhenSet() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackgroundConfig baseBackgroundConfig = DisplayBackgroundConfig.builder()
            .display("true").color("#ff0000").x("30").y("40").width("650").height("490").build();
        DisplayBackgroundConfig videoBackgroundConfig = DisplayBackgroundConfig.builder()
            .display("false").color("#0000ff").x("50").y("60").width("660").height("500").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo, videoCapture);

        DisplayBackground background = DisplayBackground
            .build(displayInfo, baseBackgroundConfig, videoBackgroundConfig, expMap);

        assertThat(background.isDisplay()).isFalse();
        assertThat(background.getColor()).isEqualTo(new Color(0, 0, 255));
        assertThat(background.getX()).isEqualTo(50);
        assertThat(background.getY()).isEqualTo(60);
        assertThat(background.getWidth()).isEqualTo(660);
        assertThat(background.getHeight()).isEqualTo(500);
    }

    @Test
    public void shouldUseBaseCaptureWhenSetWithSimpleCalcuations() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackgroundConfig baseBackgroundConfig = DisplayBackgroundConfig.builder()
            .display("true").color("#00ff00").x("captureX * 2").y("(captureY * 2) + 10")
            .width("captureWidth * 10").height("(captureHeight + 20) / 2").build();
        DisplayBackgroundConfig videoBackgroundConfig = null;
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo, videoCapture);

        DisplayBackground background = DisplayBackground
            .build(displayInfo, baseBackgroundConfig, videoBackgroundConfig, expMap);

        assertThat(background.isDisplay()).isTrue();
        assertThat(background.getColor()).isEqualTo(new Color(0, 255, 0));
        assertThat(background.getX()).isEqualTo(2);
        assertThat(background.getY()).isEqualTo(14);
        assertThat(background.getWidth()).isEqualTo(30);
        assertThat(background.getHeight()).isEqualTo(12);
    }

    @Test
    public void shouldUseVideoCaptureWhenSetWithCalcuations() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackgroundConfig baseBackgroundConfig = DisplayBackgroundConfig.builder()
            .display("true").color("#00ff00").x("captureX * 2").y("(captureY * 2) + 10")
            .width("captureWidth * 10").height("(captureHeight + 20) / 2").build();
        DisplayBackgroundConfig videoBackgroundConfig = DisplayBackgroundConfig.builder()
            .display("false").color("#00ffff").x("displayX * 2").y("(displayY * 2) + 10")
            .width("displayWidth / 2").height("(displayHeight + 20) / 2").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo, videoCapture);

        DisplayBackground background = DisplayBackground
            .build(displayInfo, baseBackgroundConfig, videoBackgroundConfig, expMap);

        assertThat(background.isDisplay()).isFalse();
        assertThat(background.getColor()).isEqualTo(new Color(0, 255, 255));
        assertThat(background.getX()).isEqualTo(20);
        assertThat(background.getY()).isEqualTo(50);
        assertThat(background.getWidth()).isEqualTo(320);
        assertThat(background.getHeight()).isEqualTo(250);
    }

    @Test
    public void shouldUseBothBaseAndVideoCaptureWithPartialOverrides() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackgroundConfig baseBackgroundConfig = DisplayBackgroundConfig.builder()
            .display("true").color("#ffff00").x("captureX * 2").y("")
            .width("captureWidth * 10").height(null).build();
        DisplayBackgroundConfig videoBackgroundConfig = DisplayBackgroundConfig.builder()
            .display("").color(null).x(null).y("(displayY * 2) + 10")
            .width("").height("(displayHeight + 20) / 2").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo, videoCapture);

        DisplayBackground background = DisplayBackground
            .build(displayInfo, baseBackgroundConfig, videoBackgroundConfig, expMap);

        assertThat(background.isDisplay()).isTrue();
        assertThat(background.getColor()).isEqualTo(new Color(255, 255, 0));
        assertThat(background.getX()).isEqualTo(2);
        assertThat(background.getY()).isEqualTo(50);
        assertThat(background.getWidth()).isEqualTo(30);
        assertThat(background.getHeight()).isEqualTo(250);
    }

    @Test(expected = VideoConfigurationException.class)
    public void shouldNotAllowNegativeWidthOnBaseConfig() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackgroundConfig baseBackgroundConfig = DisplayBackgroundConfig.builder()
            .x("70 / 2").y("").width(" -60").height(null).build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo, videoCapture);

        DisplayBackground.build(displayInfo, baseBackgroundConfig, null, expMap);
    }

    @Test(expected = VideoConfigurationException.class)
    public void shouldNotAllowNegativeHeightOnVideoConfig() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackgroundConfig videoBackgroundConfig = DisplayBackgroundConfig.builder()
            .x(null).y("(10 * 2) + 10").width("").height("-1").build();
        Map<String, Integer> expMap = ExpressionUtils.getExpressionMap(displayInfo, videoCapture);

        DisplayBackground.build(displayInfo, null, videoBackgroundConfig, expMap);
    }

}
