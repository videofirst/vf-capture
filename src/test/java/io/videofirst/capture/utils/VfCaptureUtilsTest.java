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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Collection of useful high-level static utility methods.
 *
 * @author Bob Marks
 */
public class VfCaptureUtilsTest {

    // ---------------------------------------------------------------------------------------------
    // VfCaptureUtils.nullTrim
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldNullTrim() {

        assertThat(VfCaptureUtils.nullTrim(null)).isNull();
        assertThat(VfCaptureUtils.nullTrim(" trim me ")).isEqualTo("trim me");
    }

    // ---------------------------------------------------------------------------------------------
    // VfCaptureUtils.mergeMaps
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldShouldMerge3Maps() {

        Map<String, String> m1 = ImmutableMap.of("a", "1", "b", "2");
        Map<String, String> m2 = ImmutableMap.of("b", "3", "c", "4");
        Map<String, String> m3 = ImmutableMap.of("a", "5", "d", "6");

        Map<String, String> mergedMap = VfCaptureUtils.mergeMaps(m1, m2, m3);

        assertThat(mergedMap).isEqualTo(ImmutableMap.of("a", "5", "b", "3", "c", "4", "d", "6"));
    }

    @Test
    public void shouldShouldMerge2MapsAnd2Nulls() {

        Map<String, String> m2 = ImmutableMap.of("b", "3", "c", "4");
        Map<String, String> m3 = ImmutableMap.of("a", "5", "b", "2");

        Map<String, String> mergedMap = VfCaptureUtils.mergeMaps(null, m2, m3, null);

        assertThat(mergedMap).isEqualTo(ImmutableMap.of("a", "5", "b", "2", "c", "4"));
    }

    @Test
    public void shouldShouldMergeHandleNulls() {

        Map<String, String> mergedMap = VfCaptureUtils.mergeMaps(null, null);

        assertThat(mergedMap).isEmpty();
    }

    // ---------------------------------------------------------------------------------------------
    // VfCaptureUtils.getFolderFriendly
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldGetFolderFriendly() {

        assertThat(VfCaptureUtils.getFolderFriendly("a b 1 2 c d"))
            .isEqualTo("a-b-1-2-c-d");
        assertThat(VfCaptureUtils.getFolderFriendly("  -- A b 1 2 C D - - "))
            .isEqualTo("a-b-1-2-c-d");
        assertThat(VfCaptureUtils.getFolderFriendly(" _- a -- b__c -- d "))
            .isEqualTo("a-b_c-d");
        assertThat(VfCaptureUtils.getFolderFriendly("2015-01-02_12-13-14_abcd12"))
            .isEqualTo("2015-01-02_12-13-14_abcd12");
        assertThat(VfCaptureUtils.getFolderFriendly("2015-01-02_12-13-14_abcd12!£$%^&\""))
            .isEqualTo("2015-01-02_12-13-14_abcd12");

        //  null check
        assertThat(VfCaptureUtils.getFolderFriendly(null)).isNull();
    }

    // ---------------------------------------------------------------------------------------------
    // VfCaptureUtils.getFolderFriendlyList
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldGetFolderFriendlyList() {
        List<String> folders = VfCaptureUtils.getFolderFriendlyList(
            " IBM Search ", " Advanced-Search ", "Search by country!!!",
            "2015-01-02_12-13-14_abcd12");

        assertThat(folders).isEqualTo(asList("ibm-search", "advanced-search",
            "search-by-country", "2015-01-02_12-13-14_abcd12"));
    }

    @Test
    public void shouldGetFolderFriendlyListNullsAndEmpty() {
        assertThat(VfCaptureUtils.getFolderFriendlyList(null, " a ", "", " b ", null, " c ", " "))
            .isEqualTo(asList("a", "b", "c"));
    }

    // ---------------------------------------------------------------------------------------------
    // VfCaptureUtils.generateRandomLowersAndNums
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldGenerateRandomLowersAndNums() {
        assertThat(VfCaptureUtils.generateRandomLowersAndNums(4)).matches("[a-z0-9]{4}");
        assertThat(VfCaptureUtils.generateRandomLowersAndNums(10)).matches("[a-z0-9]{10}");
    }

    // ---------------------------------------------------------------------------------------------
    // VfCaptureUtils.generateId
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldGenerateId() {
        LocalDateTime ts = LocalDateTime.of(2015, 1, 2, 12, 13, 14);
        assertThat(VfCaptureUtils.generateId(ts)).matches("2015-01-02_12-13-14_[a-z0-9]{6}");
    }

    // ---------------------------------------------------------------------------------------------
    // VfCaptureUtils.generateId
    // ---------------------------------------------------------------------------------------------

    @Test
    public void shouldConvertToList() {
        assertThat(VfCaptureUtils.convertToList("a|b|c")).containsExactly("a", "b", "c");
        assertThat(VfCaptureUtils.convertToList(" a | b | c ")).containsExactly("a", "b", "c");
        assertThat(VfCaptureUtils.convertToList("  ")).isNull();
        assertThat(VfCaptureUtils.convertToList(null)).isNull();
    }

}
