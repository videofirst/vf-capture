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

import static java.util.Arrays.asList;

import co.videofirst.vft.capture.configuration.properties.SecurityConfig;
import co.videofirst.vft.capture.configuration.properties.VftConfig;
import co.videofirst.vft.capture.exception.InvalidSecurityException;
import co.videofirst.vft.capture.security.EncryptedLockOutSupportAuthenticationProvider;
import co.videofirst.vft.capture.security.LoginAttemptsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring security configuration.
 *
 * @author Bob Marks
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    // Constants

    public static final int BCRYPT_STRENGTH = 10;
    public static final String ROLE_USER = "user";

    /**
     * API spring security.
     */
    @RequiredArgsConstructor
    @Configuration
    @Order(1)
    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

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

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(asList(config.getSecurity().getAllowedOrigins()));
            configuration.addAllowedHeader("*");
            configuration.addAllowedMethod("*");
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
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
                 .cors()
             .and()
                 .authorizeRequests()
                 .antMatchers("/api/**", "/docs",
                     "/v2/api-docs", "/swagger-resources", "/swagger-resources/**",
                     "/swagger-ui.html", "/webjars/**"
                 ).authenticated()
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

    /**
     * UI security
     */
    @Configuration
    @Order(2)
    public static class UiWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                .cors()
            .and()
                .antMatcher("/**")
                .anonymous();
            // @formatter:on
        }
    }

}
