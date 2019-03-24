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
package co.videofirst.vft.capture.model.display;

import static co.videofirst.vft.capture.model.display.DisplayText.DEFAULT_TEXT_COLOR;
import static co.videofirst.vft.capture.model.display.DisplayText.DEFAULT_TEXT_DISPLAY;
import static co.videofirst.vft.capture.model.display.DisplayText.DEFAULT_TEXT_FONT_SIZE;
import static co.videofirst.vft.capture.utils.ExpressionUtils.getExpressionMap;
import static org.assertj.core.api.Assertions.assertThat;

import co.videofirst.vft.capture.configuration.properties.DisplayTextConfig;
import co.videofirst.vft.capture.model.info.DisplayInfo;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Unit test to test the methods of DisplayText.
 *
 * @author Bob Marks
 */
public class DisplayTextTest {

    @Test
    public void shouldUseDisplayInfoWhenBothBaseAndVideoTextConfigAreNull() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = null;
        DisplayBackground displayBackground = null;
        DisplayTextConfig baseTextConfig = null;
        DisplayTextConfig videoTextConfig = null;
        Map<String, Integer> expMap = getExpressionMap(displayInfo, videoCapture,
            displayBackground);

        DisplayText text = DisplayText
            .build(displayInfo, baseTextConfig, videoTextConfig, expMap);

        assertThat(text.getX()).isEqualTo(10);
        assertThat(text.getY()).isEqualTo(20);
        assertThat(text.isDisplay()).isEqualTo(DEFAULT_TEXT_DISPLAY);
        assertThat(text.getFontSize()).isEqualTo(DEFAULT_TEXT_FONT_SIZE);
        assertThat(text.getColor()).isEqualTo(DEFAULT_TEXT_COLOR);
    }

    @Test
    public void shouldUseDisplayInfoWhenBothBaseAndVideoTextConfigFieldsAreNull() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayTextConfig baseTextConfig = DisplayTextConfig.builder().build();
        DisplayTextConfig videoTextConfig = DisplayTextConfig.builder().build();
        Map<String, Integer> expMap = new HashMap<>();

        DisplayText text = DisplayText
            .build(displayInfo, baseTextConfig, videoTextConfig, expMap);

        assertThat(text.isDisplay()).isEqualTo(DEFAULT_TEXT_DISPLAY);
        assertThat(text.getColor()).isEqualTo(DEFAULT_TEXT_COLOR);
        assertThat(text.getX()).isEqualTo(10);
        assertThat(text.getY()).isEqualTo(20);
        assertThat(text.getFontSize()).isEqualTo(DEFAULT_TEXT_FONT_SIZE);
    }

    @Test
    public void shouldUseDisplayInfoWhenBothBaseAndVideBackgroundeConfigFieldsAreEmpty() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackground displayBackground = DisplayBackground.builder().x(5).y(6).width(7)
            .height(8).build();
        DisplayTextConfig baseTextConfig = DisplayTextConfig.builder()
            .display("").color("").x("").y("").fontSize("").build();
        DisplayTextConfig videoTextConfig = DisplayTextConfig.builder()
            .display("").color("").x("").y("").fontSize("").build();
        Map<String, Integer> expMap = getExpressionMap(displayInfo, videoCapture,
            displayBackground);

        DisplayText text = DisplayText
            .build(displayInfo, baseTextConfig, videoTextConfig, expMap);

        assertThat(text.isDisplay()).isEqualTo(DEFAULT_TEXT_DISPLAY);
        assertThat(text.getColor()).isEqualTo(DEFAULT_TEXT_COLOR);
        assertThat(text.getX()).isEqualTo(10);
        assertThat(text.getY()).isEqualTo(20);
        assertThat(text.getFontSize()).isEqualTo(DEFAULT_TEXT_FONT_SIZE);

    }

    @Test
    public void shouldUseBaseCaptureWhenSet() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackground displayBackground = DisplayBackground.builder().x(5).y(6).width(7)
            .height(8).build();
        DisplayTextConfig baseTextConfig = DisplayTextConfig.builder()
            .display("true").color("#FF00FF").x("30").y("40").fontSize("18").build();
        DisplayTextConfig videoTextConfig = null;
        Map<String, Integer> expMap = getExpressionMap(displayInfo, videoCapture,
            displayBackground);

        DisplayText text = DisplayText
            .build(displayInfo, baseTextConfig, videoTextConfig, expMap);

        assertThat(text.isDisplay()).isTrue();
        assertThat(text.getColor()).isEqualTo(new Color(255, 0, 255));
        assertThat(text.getX()).isEqualTo(30);
        assertThat(text.getY()).isEqualTo(40);
        assertThat(text.getFontSize()).isEqualTo(18);
    }

    @Test
    public void shouldUseVideoCaptureWhenSet() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackground displayBackground = DisplayBackground.builder().x(5).y(6).width(7)
            .height(8).build();
        DisplayTextConfig baseTextConfig = DisplayTextConfig.builder()
            .display("true").color("#FF00FF").x("30").y("40").fontSize("18").build();
        DisplayTextConfig videoTextConfig = DisplayTextConfig.builder()
            .display("false").color("#00FFFF").x("50").y("60").fontSize("21").build();
        Map<String, Integer> expMap = getExpressionMap(displayInfo, videoCapture,
            displayBackground);

        DisplayText text = DisplayText
            .build(displayInfo, baseTextConfig, videoTextConfig, expMap);

        assertThat(text.isDisplay()).isFalse();
        assertThat(text.getColor()).isEqualTo(new Color(0, 255, 255));
        assertThat(text.getX()).isEqualTo(50);
        assertThat(text.getY()).isEqualTo(60);
        assertThat(text.getFontSize()).isEqualTo(21);
    }

    @Test
    public void shouldUseBaseCaptureWhenSetWithSimpleCalcuations() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackground displayBackground = DisplayBackground.builder().x(5).y(6).width(7)
            .height(8).build();
        DisplayTextConfig baseTextConfig = DisplayTextConfig.builder()
            .display("true").color("#00ffff").x("captureX * 2").y("(captureY * 2) + 10")
            .fontSize("21").build();
        DisplayTextConfig videoTextConfig = null;
        Map<String, Integer> expMap = getExpressionMap(displayInfo, videoCapture,
            displayBackground);

        DisplayText text = DisplayText
            .build(displayInfo, baseTextConfig, videoTextConfig, expMap);

        assertThat(text.isDisplay()).isTrue();
        assertThat(text.getColor()).isEqualTo(new Color(0, 255, 255));
        assertThat(text.getX()).isEqualTo(2);
        assertThat(text.getY()).isEqualTo(14);
        assertThat(text.getFontSize()).isEqualTo(21);
    }

    @Test
    public void shouldUseVideoCaptureWhenSetWithCalcuations() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackground displayBackground = DisplayBackground.builder().x(5).y(6).width(7)
            .height(8).build();
        DisplayTextConfig baseTextConfig = DisplayTextConfig.builder()
            .display("true").color("#00ff00").x("captureX * 2").y("(captureY * 2) + 10")
            .fontSize("21").build();
        DisplayTextConfig videoTextConfig = DisplayTextConfig.builder()
            .display("false").color("#00ffff").x("displayX * 2").y("(backgroundY * 2) + 10")
            .fontSize("28").build();
        Map<String, Integer> expMap = getExpressionMap(displayInfo, videoCapture,
            displayBackground);

        DisplayText text = DisplayText
            .build(displayInfo, baseTextConfig, videoTextConfig, expMap);

        assertThat(text.isDisplay()).isFalse();
        assertThat(text.getColor()).isEqualTo(new Color(0, 255, 255));
        assertThat(text.getX()).isEqualTo(20);
        assertThat(text.getY()).isEqualTo(22);
        assertThat(text.getFontSize()).isEqualTo(28);
    }

    @Test
    public void shouldUseBothBaseAndVideoCaptureWithPartialOverrides() {
        DisplayInfo displayInfo = DisplayInfo.builder().x(10).y(20).width(640).height(480).build();
        DisplayCapture videoCapture = DisplayCapture.builder().x(1).y(2).width(3).height(4).build();
        DisplayBackground displayBackground = DisplayBackground.builder().x(5).y(6).width(7)
            .height(8).build();
        DisplayTextConfig baseTextConfig = DisplayTextConfig.builder()
            .display("true").color("#ffff00").x("captureX * 2").y("")
            .fontSize("23").build();
        DisplayTextConfig videoTextConfig = DisplayTextConfig.builder()
            .display("").color(null).x(null).y("(displayY * 2) + 10")
            .fontSize("").build();

        Map<String, Integer> expMap = getExpressionMap(displayInfo, videoCapture,
            displayBackground);

        DisplayText text = DisplayText
            .build(displayInfo, baseTextConfig, videoTextConfig, expMap);

        assertThat(text.isDisplay()).isTrue();
        assertThat(text.getColor()).isEqualTo(new Color(255, 255, 0));
        assertThat(text.getX()).isEqualTo(2);
        assertThat(text.getY()).isEqualTo(50);
        assertThat(text.getFontSize()).isEqualTo(23);
    }

}
