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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRevocationServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RefreshTokenRevocationService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenRevocationService(redisTemplate);
    }

    @Test
    void revoke_storesJtiWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.revoke("jti-1", 3600);

        verify(valueOperations).set(eq("revoked:refresh-token:jti-1"), eq("1"), eq(Duration.ofSeconds(3600)));
    }

    @Test
    void revoke_doesNothing_whenJtiIsNull() {
        service.revoke(null, 3600);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void isRevoked_true_whenKeyExists() {
        when(redisTemplate.hasKey("revoked:refresh-token:jti-1")).thenReturn(true);

        assertThat(service.isRevoked("jti-1")).isTrue();
    }

    @Test
    void isRevoked_false_whenKeyAbsent() {
        when(redisTemplate.hasKey(any())).thenReturn(false);

        assertThat(service.isRevoked("jti-1")).isFalse();
    }

    @Test
    void isRevoked_false_whenJtiIsNull() {
        assertThat(service.isRevoked(null)).isFalse();
    }
}
