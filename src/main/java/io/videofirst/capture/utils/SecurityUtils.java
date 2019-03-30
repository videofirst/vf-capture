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

import io.videofirst.capture.configuration.SecurityConfiguration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Collection of security utilies.
 *
 * @author Bob Marks
 */
public class SecurityUtils {

    /**
     * Create password if first command arg is `-pwd`.
     */
    public static void checkCreatePassword(String[] args) {
        // Check to see if we're trying to change password
        if ((args.length == 1 || args.length == 2) && "-pwd".equals(args[0])) {
            System.out.println("=============================");
            System.out.println("VF CAPTURE - PASSWORD ENCODER");
            System.out.println("=============================");

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(
                SecurityConfiguration.BCRYPT_STRENGTH);
            if (args.length == 2) {
                System.out
                    .println("\nEncoded output [ " + passwordEncoder.encode(args[1]) + " ]\n");
                System.exit(0);
            }

            while (true) {
                try {
                    System.out.print("\nPlease enter password (or q to exit): - ");
                    System.out.flush();
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String input = br.readLine();

                    if ("q".equalsIgnoreCase(input)) {
                        System.exit(0);
                    }
                    System.out
                        .println("\nEncoded output [ " + passwordEncoder.encode(input) + " ]\n");
                } catch (IOException e) {
                    System.out.println("Error reading line " + e.getMessage());
                }
            }
        }
    }

}
