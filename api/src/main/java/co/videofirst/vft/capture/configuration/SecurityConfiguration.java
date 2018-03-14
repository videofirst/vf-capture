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
package co.videofirst.vft.capture.configuration;

import co.videofirst.vft.capture.configuration.properties.SecurityConfig;
import co.videofirst.vft.capture.configuration.properties.VftConfig;
import co.videofirst.vft.capture.exception.InvalidSecurityException;
import co.videofirst.vft.capture.security.EncryptedLockOutSupportAuthenticationProvider;
import co.videofirst.vft.capture.security.LoginAttemptsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring security configuration.
 *
 * @author Bob Marks
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    // Constants

    public static final String ROLE_USER = "user";
    public static final int BCRYPT_STRENGTH = 10;

    // Injected fields

    private final VftConfig config;
    private final LoginAttemptsService loginAttemptsService;

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    @Bean
    public AuthenticationProvider getEncryptedLockOutSupportAuthenticationProvider() {
        return new EncryptedLockOutSupportAuthenticationProvider(config, getPasswordEncoder(),
            loginAttemptsService);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        SecurityConfig securityConfig = config.getSecurity();
        String username = securityConfig.getUser();
        String password = securityConfig.getPass();

        validate(username, password);
        auth.authenticationProvider(getEncryptedLockOutSupportAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .authorizeRequests()
            .anyRequest()
            .fullyAuthenticated()
        .and()
            .httpBasic()
        .and()
            .csrf().disable();
        // @formatter:on
    }

    // Private methods

    /**
     * Validation.
     */
    private void validate(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new InvalidSecurityException(
                "Please specify a valid [ vft_config.security.user ]");
        }
        if (password == null || password.isEmpty()) {
            throw new InvalidSecurityException(
                "Please specify a valid [ vft_config.security.pass ]");
        }
    }
}
