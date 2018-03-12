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

import static co.videofirst.vft.capture.model.display.DisplayBorder.DEFAULT_BORDER_COLOR;
import static co.videofirst.vft.capture.model.display.DisplayBorder.DEFAULT_PADDING;
import static co.videofirst.vft.capture.model.display.DisplayBorder.DEFAULT_WIDTH;
import static org.assertj.core.api.Assertions.assertThat;

import co.videofirst.vft.capture.configuration.properties.DisplayBorderConfig;
import java.awt.Color;
import org.junit.Test;

/**
 * Unit test to test the methods of DisplayBackground.
 *
 * @author Bob Marks
 */
public class DisplayBorderTest {

    // border.show

    @Test
    public void shouldNotDisplayForNullObject() {
        DisplayBorder border = DisplayBorder.build(null, null);

        assertThat(border.isDisplay()).isFalse();
        assertThat(border.getColor()).isEqualTo(DEFAULT_BORDER_COLOR);
        assertThat(border.getPadding()).isEqualTo(DEFAULT_PADDING);
        assertThat(border.getWidth()).isEqualTo(DEFAULT_WIDTH);
    }

    @Test
    public void shouldParseForBaseBorder() {
        DisplayBorderConfig baseBorderConfig = DisplayBorderConfig.builder()
            .display(" true ").color(" #ff0000 ").padding(" 3 ").width(" 8 ").build();

        DisplayBorder border = DisplayBorder.build(baseBorderConfig, null);

        assertThat(border.isDisplay()).isTrue();
        assertThat(border.getColor()).isEqualTo(new Color(255, 0, 0));
        assertThat(border.getPadding()).isEqualTo(3);
        assertThat(border.getWidth()).isEqualTo(8);
    }

    @Test
    public void shouldParseForOverrideBorder() {
        DisplayBorderConfig videoBorderConfig = DisplayBorderConfig.builder()
            .display("true").color("#00ff00").padding("5").width("7").build();

        DisplayBorder border = DisplayBorder.build(null, videoBorderConfig);

        assertThat(border.isDisplay()).isTrue();
        assertThat(border.getColor()).isEqualTo(new Color(0, 255, 0));
        assertThat(border.getPadding()).isEqualTo(5);
        assertThat(border.getWidth()).isEqualTo(7);
    }

    @Test
    public void shouldParseForOverrideBorderWhenBothSet() {
        DisplayBorderConfig baseBorderConfig = DisplayBorderConfig.builder()
            .display("true").color("#ff0000").padding("1").width("").build();
        DisplayBorderConfig videoBorderConfig = DisplayBorderConfig.builder()
            .display("false").color("#0000ff").padding("").width("4").build();

        DisplayBorder border = DisplayBorder.build(baseBorderConfig, videoBorderConfig);

        assertThat(border.isDisplay()).isFalse();
        assertThat(border.getColor()).isEqualTo(new Color(0, 0, 255));
        assertThat(border.getPadding()).isEqualTo(1);
        assertThat(border.getWidth()).isEqualTo(4);
    }

}
