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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Collection of useful static utility methods.
 *
 * @author Bob Marks
 */
public class VftUtils {

    // Constants

    private static final String RANDOM_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final DateTimeFormatter VIDEO_FORMAT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final int ID_RANDOM_LENGTH = 6;

    // Static methods

    /**
     * Trim a String but also null check check.
     */
    public static String nullTrim(String input) {
        return input != null ? input.trim() : input;
    }

    /**
     * Merge 1 or more maps of <String,String>together.
     */
    public static Map<String, String> mergeMaps(Map<String, String>... maps) {
        Map<String, String> mergedMap = new LinkedHashMap<>();
        if (maps != null) {
            Arrays.stream(maps)
                .filter(Objects::nonNull)
                .forEach(map -> map.entrySet().stream()
                    .forEach(e -> mergedMap.put(e.getKey(), e.getValue()))
                );
        }
        return mergedMap;
    }

    /**
     * Return the folder friendly of an input.
     *
     * This involves (1) converting input to lower case, (2) replacing all spaces with a dash
     * character, (3) removing all "non-word" character except for dashes and underscores and then
     * (4) if more than single dashes of underscore exists then replace with a single one and then
     * (5) trimming the the dashes / underscores from each end if they exist.
     *
     * For example, an input of " New Microsoft Product! - " will return "new-microsoft-product".
     */
    public static String getFolderFriendly(String input) {
        if (input == null) {
            return null;
        }
        String folderFriendlyInput = input.toLowerCase()
            .replace(" ", "-") // replace all spaces with a dash
            .replaceAll("[^\\w-_]+", "")  // remove all non-word chars (except dashses)
            .replaceAll("-+", "-")  // replace lots of of dashes with a single one
            .replaceAll("_+", "_")  // replace lots of of underscores with a single one
            .replaceAll("^[-_]+|[-_]+$", ""); // finally trim dashes / underscores
        return folderFriendlyInput;
    }

    /**
     * Convert a list of folder parameters into a friendly list of Strings describing a folder
     * structure.
     */
    public static List<String> getFolderFriendlyList(String... folders) {
        List<String> friendlyFolders = Arrays.stream(folders)
            .filter(Objects::nonNull)
            .filter(folder -> !folder.trim().isEmpty())
            .map(folder -> getFolderFriendly(folder))
            .collect(Collectors.toList());
        return friendlyFolders;
    }

    /**
     * Generate string using lowercase characters and numbers.
     *
     * @param length Length of random string.
     */
    public static String generateRandomLowersAndNums(int length) {
        String random = RandomStringUtils.random(length, RANDOM_CHARS);
        return random;
    }

    /**
     * Generate an Id using a combination of a date/time + a random string.
     *
     * @param dateTime Datetime to use to generate ID.
     */
    public static String generateId(LocalDateTime dateTime) {
        String id = VIDEO_FORMAT.format(dateTime) + "_" +
            generateRandomLowersAndNums(ID_RANDOM_LENGTH);
        return id;
    }

    /**
     * Take a String which contains 1 or more items, delimited by a pipe character e.g. "dave| john|
     * joe " will return a list of "dave", "john" and "joe".
     */
    public static List<String> convertToList(String input) {
        if (input != null && !input.trim().isEmpty()) {
            List<String> list = Arrays.stream(input.split("\\|"))
                .map(i -> i.trim()).collect(Collectors.toList());
            return list;
        }
        return null;
    }
}
