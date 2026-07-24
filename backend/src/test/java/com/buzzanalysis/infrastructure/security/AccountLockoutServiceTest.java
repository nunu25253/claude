package com.buzzanalysis.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** {@link AccountLockoutService} の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class AccountLockoutServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AccountLockoutService service;

    @BeforeEach
    void setUp() {
        AccountLockoutProperties properties = new AccountLockoutProperties();
        properties.setMaxFailedAttempts(3);
        properties.setFailureWindowMinutes(15);
        properties.setLockoutMinutes(30);
        service = new AccountLockoutService(redisTemplate, properties);
    }

    @Test
    void isLocked_returnsFalse_whenNoLockKeyExists() {
        when(redisTemplate.hasKey("lockout:locked:user@example.com")).thenReturn(false);

        assertThat(service.isLocked("user@example.com")).isFalse();
    }

    @Test
    void isLocked_returnsTrue_whenLockKeyExists() {
        when(redisTemplate.hasKey("lockout:locked:user@example.com")).thenReturn(true);

        assertThat(service.isLocked("user@example.com")).isTrue();
    }

    @Test
    void isLocked_normalizesEmailCaseAndWhitespace() {
        when(redisTemplate.hasKey("lockout:locked:user@example.com")).thenReturn(true);

        assertThat(service.isLocked("  User@Example.com  ")).isTrue();
    }

    @Test
    void recordFailure_setsExpiryOnlyOnFirstFailure_andDoesNotLockBelowThreshold() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("lockout:failures:user@example.com")).thenReturn(1L);

        service.recordFailure("user@example.com");

        verify(redisTemplate).expire("lockout:failures:user@example.com", Duration.ofMinutes(15));
        verify(valueOperations, never()).set(eq("lockout:locked:user@example.com"), anyString(), any(Duration.class));
    }

    @Test
    void recordFailure_locksAccount_whenThresholdReached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("lockout:failures:user@example.com")).thenReturn(3L);

        service.recordFailure("user@example.com");

        verify(valueOperations).set("lockout:locked:user@example.com", "1", Duration.ofMinutes(30));
    }

    @Test
    void recordSuccess_deletesFailureAndLockKeys() {
        service.recordSuccess("User@Example.com");

        verify(redisTemplate).delete("lockout:failures:user@example.com");
        verify(redisTemplate).delete("lockout:locked:user@example.com");
    }
}
