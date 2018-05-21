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
package co.videofirst.vft.capture.utils;

import co.videofirst.vft.capture.exception.VideoConfigurationException;
import com.udojava.evalex.Expression;
import java.awt.Color;
import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of use static methods related around configuration.
 *
 * @author Bob Marks
 */
public class ConfigUtils {

    // ---------------------------------------------------------------------------------------------
    // Parse Colors
    // ---------------------------------------------------------------------------------------------

    private static final Pattern HEX_COLOR_REGEX = Pattern
        .compile("^#[A-Fa-f0-9]{6}$");

    /**
     * Parse a color object from a String - currently only supports hex i.e. must start with a hash
     * `#` character.
     */
    public static Color parseColor(String colorStr) {
        if (colorStr == null || colorStr.trim().isEmpty()) {
            return null;
        }

        colorStr = colorStr.trim();
        Matcher matcher = HEX_COLOR_REGEX.matcher(colorStr);
        if (matcher.matches()) {
            return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
        }
        throw new VideoConfigurationException(
            "Invalid color input [ " + colorStr + " ] must be a valid hex color e.g. #ffaa00");
    }

    /**
     * Parse colors.  The override color gets priority over the base color.
     */
    public static Color parseColors(String baseColorStr, String overrideColorStr,
        Color defaultColor) {
        Color color = parseColor(overrideColorStr);
        if (color == null) {
            color = parseColor(baseColorStr);
        }
        return color == null ? defaultColor : color;
    }

    // ---------------------------------------------------------------------------------------------
    // Parse Integers
    // ---------------------------------------------------------------------------------------------

    public static Integer parseInt(String intStr) {
        if (intStr == null || intStr.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(intStr.trim());
        } catch (NumberFormatException nfe) {
            throw new VideoConfigurationException(
                "Invalid integer number [ " + intStr + " ]");
        }
    }

    public static Integer parseInts(String baseIntStr, String overrideIntStr,
        Integer defaultValue) {
        Integer integer = parseInt(overrideIntStr);
        if (integer == null) {
            integer = parseInt(baseIntStr);
        }
        return integer != null ? integer : defaultValue;
    }

    // ---------------------------------------------------------------------------------------------
    // Parse Integers (with expressions e.g. "x * 2 + y")
    // ---------------------------------------------------------------------------------------------

    public static Integer parseExpInt(String intStr, Map<String, Integer> expressionParams) {
        if (intStr == null || intStr.trim().isEmpty()) {
            return null;
        }

        Expression expression = new Expression(intStr);
        expressionParams.forEach((k, v) -> expression.and(k, new BigDecimal(v)));
        BigDecimal result = expression.eval();
        return result.intValue();
    }

    public static Integer parseExpInts(String baseIntStr, String overrideIntStr,
        Integer defaultValue, Map<String, Integer> expParams) {
        Integer integer = parseExpInt(overrideIntStr, expParams);
        if (integer == null) {
            integer = parseExpInt(baseIntStr, expParams);
        }
        return integer != null ? integer : defaultValue;
    }

    // ---------------------------------------------------------------------------------------------
    // Parse Booleans
    // ---------------------------------------------------------------------------------------------

    public static Boolean parseBoolean(String booleanStr) {
        if (booleanStr == null || booleanStr.trim().isEmpty()) {
            return null;
        }
        booleanStr = booleanStr.trim();
        if ("true".equalsIgnoreCase(booleanStr)) {
            return true;
        }
        if ("false".equalsIgnoreCase(booleanStr)) {
            return false;
        }
        throw new VideoConfigurationException(
            "Invalid boolean  [ " + booleanStr + " ]");
    }

    public static Boolean parseBooleans(String baseBooleanStr, String overrideBooleanStr,
        Boolean defaultValue) {
        Boolean bool = parseBoolean(overrideBooleanStr);
        if (bool == null) {
            bool = parseBoolean(baseBooleanStr);
        }
        return bool != null ? bool : defaultValue;
    }

    // ---------------------------------------------------------------------------------------------
    // Parse String
    // ---------------------------------------------------------------------------------------------

    public static String parseString(String baseString, String overrideString) {
        if (baseString == null || baseString.trim().isEmpty()) {

            if (overrideString == null || overrideString.trim().isEmpty()) {
                return null;
            }
            return overrideString.trim();
        }

        return baseString.trim();
    }

}
