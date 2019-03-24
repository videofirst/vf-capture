/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Video First Software
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
package co.videofirst.vft.capture.security;

import co.videofirst.vft.capture.configuration.properties.VftConfig;
import co.videofirst.vft.capture.exception.LockOutException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * See [ http://www.baeldung.com/spring-security-block-brute-force-authentication-attempts ] for the
 * original inspiration.
 *
 * Modified so config values are injected and accounts are locked out for seconds not days.  Also,
 * moved the HttpServletRequest here instead of the higher level security class which simplifies the
 * code / method signatures.
 */
@Slf4j
@Service
public class LoginAttemptsService {

    // Injected fields

    private final long lockOutAttempts;
    private final long lockOutInSeconds;

    @Autowired
    private HttpServletRequest request;

    // Other fields

    private final LoadingCache<String, LoginAttempts> attemptsCache;

    public LoginAttemptsService(VftConfig config) {
        lockOutAttempts = config.getSecurity().getLockOutAttempts();
        lockOutInSeconds = config.getSecurity().getLockOutInSeconds();

        attemptsCache = CacheBuilder.newBuilder().
            expireAfterWrite(lockOutInSeconds, TimeUnit.SECONDS)
            .build(new CacheLoader<String, LoginAttempts>() {
                @Override
                public LoginAttempts load(String key) {
                    return new LoginAttempts();
                }
            });
    }

    public void loginSucceeded() {
        String ip = getClientIp();
        attemptsCache.invalidate(ip);
    }

    public void loginFailed() {
        String ip = getClientIp();
        LoginAttempts loginAttempts;
        try {
            loginAttempts = attemptsCache.get(ip);
        } catch (ExecutionException e) {
            loginAttempts = new LoginAttempts();
        }
        loginAttempts = loginAttempts.increment();
        attemptsCache.put(ip, loginAttempts);
    }

    public void checkBlocked() {
        String ip = getClientIp();
        try {
            LoginAttempts loginAttempts = attemptsCache.get(ip);
            if (loginAttempts.getAttempts() >= lockOutAttempts) {
                long remainingLockedOutSeconds = lockOutInSeconds - loginAttempts.getFirstAttempt()
                    .until(LocalDateTime.now(), ChronoUnit.SECONDS);
                if (remainingLockedOutSeconds < 0) {
                    attemptsCache.invalidate(ip);
                    return;
                }
                throw new LockOutException(
                    "Too many invalid login attempts - access is now blocked for [ "
                        + remainingLockedOutSeconds + " ] seconds for ip [ " + ip + " ]");
            }

        } catch (ExecutionException e) {
            log.warn("Exception exception putting " + ip + " into cache", e);
        }
    }

    // Private methods

    private String getClientIp() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}