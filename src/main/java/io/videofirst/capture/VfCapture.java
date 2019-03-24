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
package io.videofirst.capture;

import com.bulenkov.darcula.DarculaLaf;
import io.videofirst.capture.configuration.SecurityConfiguration;
import io.videofirst.capture.exception.CaptureException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.basic.BasicLookAndFeel;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Main application class for VF Capture.
 *
 * @author Bob Marks
 */
@EnableScheduling
@SpringBootApplication
public class VfCapture {

    public static void main(String[] args) {

        // https://stackoverflow.com/questions/36634281/list-of-swagger-ui-alternatives

        setUiLookAndFeel();

        checkCreatePassword(args);

        SpringApplicationBuilder builder = new SpringApplicationBuilder(VfCapture.class);
        builder.headless(false).properties("spring.config.name:capture").run(args);
    }

    /**
     * Set look and feel (needs to be done before application loads up).
     */
    private static void setUiLookAndFeel() {
        try {
            BasicLookAndFeel darcula = new DarculaLaf();
            UIManager.setLookAndFeel(darcula);
        } catch (UnsupportedLookAndFeelException ex) {
            throw new CaptureException("Look and feel not supported");
        }
    }

    /**
     * Create password if first command arg is `-pwd`.
     */
    private static void checkCreatePassword(String[] args) {
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
