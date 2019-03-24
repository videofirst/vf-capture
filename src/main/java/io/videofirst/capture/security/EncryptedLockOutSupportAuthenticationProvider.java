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
package io.videofirst.capture.security;

import io.videofirst.capture.configuration.SecurityConfiguration;
import io.videofirst.capture.configuration.properties.CaptureConfig;
import io.videofirst.capture.configuration.properties.SecurityConfig;
import io.videofirst.capture.enums.SecurityType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Authentication provider which supports (1)  encryption and (2) lock out support via the
 * LoginAttemptsService.
 *
 * @author Bob Marks
 */
@Slf4j
@RequiredArgsConstructor
public class EncryptedLockOutSupportAuthenticationProvider implements AuthenticationProvider {

    // Injected fields

    private final CaptureConfig config;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptsService loginAttemptsService;

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {

        // First then we do is check if we're locked out.
        loginAttemptsService.checkBlocked();

        // Read security configuration
        SecurityConfig securityConfig = config.getSecurity();
        SecurityType securityType = securityConfig.getType();
        String securityUser = securityConfig.getUser().trim();
        String securityPass = securityConfig.getPass().trim();

        // Read items from user
        String providedUser = authentication.getName().trim();
        String providedPass = authentication.getCredentials().toString().trim();

        // Check if `plain` ...
        if (securityType == SecurityType.plain &&
            securityUser.equals(providedUser) &&
            securityPass.equals(providedPass)) {
            return validAuth(securityUser, authentication);
        }
        // ... or `encrypted`
        else if (securityType == SecurityType.encrypted &&
            passwordEncoder.matches(providedUser, securityUser) &&
            passwordEncoder.matches(providedUser, providedPass)) {
            return validAuth(securityUser, authentication);
        }

        invalidAuth(securityUser);
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    // Private methods

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(role));
        return Collections.unmodifiableSet(authorities);
    }

    private void invalidAuth(String securityUser) {
        log.info("User login unsuccessful " + securityUser);
        loginAttemptsService.loginFailed();
        throw new BadCredentialsException("Invalid username and password");
    }

    private Authentication validAuth(String name, Authentication authentication) {
        log.debug("User login success for " + name);
        loginAttemptsService.loginSucceeded();
        return new UsernamePasswordAuthenticationToken(name,
            authentication.getCredentials(), getAuthorities(SecurityConfiguration.ROLE_USER));
    }

}