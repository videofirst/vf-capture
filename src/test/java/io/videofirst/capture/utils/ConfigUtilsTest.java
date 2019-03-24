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
package io.videofirst.capture.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import io.videofirst.capture.exception.VideoConfigurationException;
import java.awt.Color;
import java.util.Map;
import org.junit.Test;

/**
 * Unit test to test the methods of `ConfigUtils`.
 *
 * @author Bob Marks
 */
public class ConfigUtilsTest {

    // ---------------------------------------------------------------------------------------------
    // ConfigUtils.parseColor
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldParseValidColor() {

        checkParseColor("#ff0000", 255, 0, 0);
        checkParseColor("#00ff00", 0, 255, 0);
        checkParseColor("#0000ff", 0, 0, 255);
        checkParseColor("#75C693", 117, 198, 147);
    }

    @Test
    public void shouldReturnNullForNullAndEmptyColor() {
        assertThat(ConfigUtils.parseColor(null)).isNull();
        assertThat(ConfigUtils.parseColor("")).isNull();
    }

    @Test
    public void shouldNotParseInvalidColor() {

        checkParseColorIsInvalid("#");
        checkParseColorIsInvalid("#f");
        checkParseColorIsInvalid("#ff");
        checkParseColorIsInvalid("#fff");
        checkParseColorIsInvalid("#ffff");
        checkParseColorIsInvalid("#fffff"); // 5
        checkParseColorIsInvalid("#fffffff"); // 7
        checkParseColorIsInvalid("#ffffffffff"); // 10
        checkParseColorIsInvalid("#ff000g");
    }

    // ---------------------------------------------------------------------------------------------
    // ConfigUtils.parseColors
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldParseValidColors() {

        Color defaultColor = new Color(1, 2, 3);

        // Should be base as override is null
        checkParseColors("#ff0000", null, defaultColor, 255, 0, 0);
        checkParseColors("#00ff00", null, defaultColor, 0, 255, 0);
        checkParseColors("#0000ff", "", defaultColor, 0, 0, 255);
        checkParseColors("#75C693", "", defaultColor, 117, 198, 147);

        // Should be override regardless that base is null
        checkParseColors(null, "#ff0000", defaultColor, 255, 0, 0);
        checkParseColors(null, "#00ff00", defaultColor, 0, 255, 0);
        checkParseColors("", "#0000ff", defaultColor, 0, 0, 255);
        checkParseColors("", "#75C693", defaultColor, 117, 198, 147);

        // Should be override color each time
        checkParseColors("#004400", "#ff0000", defaultColor, 255, 0, 0);
        checkParseColors("#004400", "#00ff00", defaultColor, 0, 255, 0);
        checkParseColors("#004400", "#0000ff", defaultColor, 0, 0, 255);
        checkParseColors("#004400", "#75C693", defaultColor, 117, 198, 147);
    }

    @Test
    public void shouldReturnDefaultColorForNullAndEmptyColors() {
        Color defaultColor = new Color(1, 2, 3);

        assertThat(ConfigUtils.parseColors(null, null, null)).isNull();
        assertThat(ConfigUtils.parseColors(null, "", null)).isNull();
        assertThat(ConfigUtils.parseColors("", null, null)).isNull();
        assertThat(ConfigUtils.parseColors("", "", null)).isNull();

        checkParseColors(null, null, defaultColor, 1, 2, 3);
        checkParseColors(null, "", defaultColor, 1, 2, 3);
        checkParseColors("", null, defaultColor, 1, 2, 3);
        checkParseColors("", "", defaultColor, 1, 2, 3);
    }

    @Test
    public void shouldNotParseInvalidColors() {

        Color defaultColor = new Color(1, 2, 3);

        checkParseColorsAreInvalid("#ffffff", "#", null);
        checkParseColorsAreInvalid("#ffffff", "#f", null);
        checkParseColorsAreInvalid("#ffffff", "#ff", null);
        checkParseColorsAreInvalid("#ffffff", "#fff", null);
        checkParseColorsAreInvalid("#ffffff", "#ffff", null);
        checkParseColorsAreInvalid("#ffffff", "#fffff", defaultColor); // 5
        checkParseColorsAreInvalid("#ffffff", "#fffffff", defaultColor); // 7
        checkParseColorsAreInvalid("#ffffff", "#ffffffffff", defaultColor); // 10
        checkParseColorsAreInvalid("#ffffff", "#ff000g", defaultColor);

        checkParseColorsAreInvalid("#", null, null);
        checkParseColorsAreInvalid("#f", null, null);
        checkParseColorsAreInvalid("#ff", null, null);
        checkParseColorsAreInvalid("#fff", null, null);
        checkParseColorsAreInvalid("#ffff", null, null);
        checkParseColorsAreInvalid("#fffff", "", defaultColor);
        checkParseColorsAreInvalid("#fffffff", "", defaultColor);
        checkParseColorsAreInvalid("#ffffffffff", "", defaultColor);
        checkParseColorsAreInvalid("#ff000g", "", defaultColor);
    }

    // ---------------------------------------------------------------------------------------------
    // ConfigUtils.parseInt
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldParseValidInt() {

        checkParseInt("23", 23);
        checkParseInt(" 255 ", 255);
        checkParseInt("-432", -432);
        checkParseInt(" -123", -123);
    }

    @Test
    public void shouldReturnNullForNullAndEmptyInt() {
        assertThat(ConfigUtils.parseInt(null)).isNull();
        assertThat(ConfigUtils.parseInt("")).isNull();
    }

    @Test
    public void shouldNotParseInvalidInt() {

        checkParseIntIsInvalid(" 2f ");
        checkParseIntIsInvalid(" #fsd ");
        checkParseIntIsInvalid("2.1");
    }

    // ---------------------------------------------------------------------------------------------
    // ConfigUtils.parseInts
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldParseValidInts() {

        // Should be base as override is null
        checkParseInts(" 255 ", null, null, 255);
        checkParseInts(" 1234 ", "", null, 1234);

        // Should be override regardless that base is null
        checkParseInts(null, " 255 ", null, 255);
        checkParseInts("", "1234 ", null, 1234);

        // Should be override Int each time
        checkParseInts("999", " 255 ", null, 255);
        checkParseInts("999", "1234 ", null, 1234);
    }

    @Test
    public void shouldReturnDefaultValueForNullAndEmptyInts() {
        assertThat(ConfigUtils.parseInts(null, null, 3)).isEqualTo(3);
        assertThat(ConfigUtils.parseInts(null, "", null)).isNull();
        assertThat(ConfigUtils.parseInts("", null, null)).isNull();
        assertThat(ConfigUtils.parseInts("", "", 3)).isEqualTo(3);
    }

    @Test
    public void shouldNotParseInvalidInts() {

        checkParseIntsAreInvalid(" 2f ", " 2f", 123);
        checkParseIntsAreInvalid(" #fsd ", " #fsd", null);
        checkParseIntsAreInvalid("2.1", "2.1", null);

        checkParseIntsAreInvalid(" 2f ", null, 123);
        checkParseIntsAreInvalid(" #fsd ", null, null);
        checkParseIntsAreInvalid("2.1", "", null);
        checkParseIntsAreInvalid("2.1", "", 123);
    }

    // ---------------------------------------------------------------------------------------------
    // ConfigUtils.parseExpInt
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldParseIntWithExpressions() {
        Map<String, Integer> expParams = ImmutableMap.of("x", 8, "y", 3);
        assertThat(ConfigUtils.parseExpInt(" (x / 2) + y", expParams)).isEqualTo(7);
    }

    // ---------------------------------------------------------------------------------------------
    // ConfigUtils.parseExpInts
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldParseIntsWithExpressions() {
        Map<String, Integer> expParams = ImmutableMap.of("x", 8, "y", 3);
        assertThat(ConfigUtils.parseExpInts(" (x / 4) - y", "", 23, expParams)).isEqualTo(-1);
        assertThat(ConfigUtils.parseExpInts(" (x / 2) + y", " (x * 2) - y", 23, expParams))
            .isEqualTo(13);
        assertThat(ConfigUtils.parseExpInts(null, " (x * 2) - y", 23, expParams)).isEqualTo(13);
        assertThat(ConfigUtils.parseExpInts("", " (x * 2) - y", 23, expParams)).isEqualTo(13);

        assertThat(ConfigUtils.parseExpInts(null, "", 25, expParams)).isEqualTo(25);
        assertThat(ConfigUtils.parseExpInts("", null, 26, expParams)).isEqualTo(26);
    }

    // ---------------------------------------------------------------------------------------------
    // ConfigUtils.parseBoolean
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldParseValidBoolean() {

        checkParseBoolean("true", true);
        checkParseBoolean("false", false);
        checkParseBoolean(" FALSE ", false);
        checkParseBoolean(" TRUE ", true);
        checkParseBoolean(" FaLsE ", false);
        checkParseBoolean(" TrUe ", true);
    }

    @Test
    public void shouldReturnNullForNullAndEmptyBoolean() {
        assertThat(ConfigUtils.parseBoolean(null)).isNull();
        assertThat(ConfigUtils.parseBoolean("")).isNull();
    }

    @Test
    public void shouldNotParseInvalidBoolean() {

        checkParseBooleanIsInvalid(" trueE ");
        checkParseBooleanIsInvalid(" tru ");
        checkParseBooleanIsInvalid("Fal se");
    }

    // ---------------------------------------------------------------------------------------------
    // ConfigUtils.parseString
    // ---------------------------------------------------------------------------------------------


    @Test
    public void shouldParseString() {

        assertThat(ConfigUtils.parseString("a", "b")).isEqualTo("a");
        assertThat(ConfigUtils.parseString(" a ", " b ")).isEqualTo("a");
        assertThat(ConfigUtils.parseString(" a ", "")).isEqualTo("a");
        assertThat(ConfigUtils.parseString(" a ", null)).isEqualTo("a");

        assertThat(ConfigUtils.parseString("", "b")).isEqualTo("b");
        assertThat(ConfigUtils.parseString("", " b ")).isEqualTo("b");
        assertThat(ConfigUtils.parseString(null, " b ")).isEqualTo("b");

        assertThat(ConfigUtils.parseString("  ", "   ")).isNull();
        assertThat(ConfigUtils.parseString(null, "   ")).isNull();
        assertThat(ConfigUtils.parseString("  ", null)).isNull();
        assertThat(ConfigUtils.parseString(null, null)).isNull();
    }

    // Private methods

    private void checkParseColor(String colorStr, int expectedRed, int expectedGreen,
        int expectedBlue) {
        Color color = ConfigUtils.parseColor(colorStr);
        assertThat(color.getRed()).isEqualTo(expectedRed);
        assertThat(color.getGreen()).isEqualTo(expectedGreen);
        assertThat(color.getBlue()).isEqualTo(expectedBlue);
    }

    private void checkParseColorIsInvalid(String colorStr) {
        try {
            ConfigUtils.parseColor(colorStr);
            fail("Parsing color should have failed " + colorStr);
        } catch (VideoConfigurationException ex) {
            // do nothing
        }
    }

    private void checkParseColors(String baseColorStr, String overrideColorStr, Color defaultColor,
        int expectedRed,
        int expectedGreen,
        int expectedBlue) {
        Color color = ConfigUtils.parseColors(baseColorStr, overrideColorStr, defaultColor);
        assertThat(color.getRed()).isEqualTo(expectedRed);
        assertThat(color.getGreen()).isEqualTo(expectedGreen);
        assertThat(color.getBlue()).isEqualTo(expectedBlue);
    }

    private void checkParseColorsAreInvalid(String baseColorStr, String overrideColorStr,
        Color defaultColor) {
        try {
            ConfigUtils.parseColors(baseColorStr, overrideColorStr, defaultColor);
            fail("Parsing color should have failed - baseColorStr=" + baseColorStr
                + ", overrideColorStr=" + overrideColorStr);
        } catch (VideoConfigurationException ex) {
            // do nothing
        }
    }

    private void checkParseInt(String intStr, Integer expectedInt) {
        Integer actualInt = ConfigUtils.parseInt(intStr);
        assertThat(actualInt).isEqualTo(expectedInt);
    }

    private void checkParseIntIsInvalid(String intStr) {
        try {
            ConfigUtils.parseInt(intStr);
            fail("Parsing int should have failed " + intStr);
        } catch (VideoConfigurationException ex) {
            // do nothing
        }
    }

    private void checkParseInts(String baseIntStr, String overrideIntStr, Integer defaultInt,
        Integer expectedInt) {
        Integer actualInt = ConfigUtils.parseInts(baseIntStr, overrideIntStr, defaultInt);
        assertThat(actualInt).isEqualTo(expectedInt);
    }

    private void checkParseIntsAreInvalid(String baseIntStr, String overrideIntStr,
        Integer defaultInt) {
        try {
            ConfigUtils.parseInts(baseIntStr, overrideIntStr, defaultInt);
            fail("Parsing color should have failed - baseIntStr=" + baseIntStr
                + ", overrideIntStr=" + overrideIntStr + ", defaultInt=" + defaultInt);
        } catch (VideoConfigurationException ex) {
            // do nothing
        }
    }

    private void checkParseBoolean(String booleanStr, boolean expectedBoolean) {
        Boolean actualBoolean = ConfigUtils.parseBoolean(booleanStr);
        assertThat(actualBoolean).isEqualTo(expectedBoolean);
    }

    private void checkParseBooleanIsInvalid(String booleanStr) {
        try {
            ConfigUtils.parseBoolean(booleanStr);
            fail("Parsing boolean should have failed " + booleanStr);
        } catch (VideoConfigurationException ex) {
            // do nothing
        }
    }

}
