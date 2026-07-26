package com.buzzanalysis.infrastructure.security;

import com.buzzanalysis.application.auth.TokenProvider;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link JwtTokenProvider} の単体テスト。特にログアウト時のリフレッシュトークン失効
 * (jtiクレーム＋{@link RefreshTokenRevocationService})の挙動を検証する。
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private RefreshTokenRevocationService revocationService;

    private JwtTokenProvider tokenProvider;
    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-jwt-secret-with-enough-length-0123456789");
        properties.setAccessTokenExpirationMinutes(30);
        properties.setRefreshTokenExpirationDays(14);
        tokenProvider = new JwtTokenProvider(properties, revocationService);
        user = new User(UUID.randomUUID(), "user@example.com", "hash", "User", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void validateRefreshTokenAndGetUserId_succeeds_whenNotRevoked() {
        TokenProvider.IssuedToken refresh = tokenProvider.generateRefreshToken(user);
        when(revocationService.isRevoked(anyString())).thenReturn(false);

        UUID userId = tokenProvider.validateRefreshTokenAndGetUserId(refresh.token());

        assertThat(userId).isEqualTo(user.getId());
    }

    @Test
    void validateRefreshTokenAndGetUserId_throws_whenRevoked() {
        TokenProvider.IssuedToken refresh = tokenProvider.generateRefreshToken(user);
        when(revocationService.isRevoked(anyString())).thenReturn(true);

        assertThatThrownBy(() -> tokenProvider.validateRefreshTokenAndGetUserId(refresh.token()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void validateRefreshTokenAndGetUserId_throws_whenGivenAnAccessToken() {
        TokenProvider.IssuedToken access = tokenProvider.generateAccessToken(user);

        assertThatThrownBy(() -> tokenProvider.validateRefreshTokenAndGetUserId(access.token()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void revokeRefreshToken_recordsJtiWithRemainingTtl() {
        TokenProvider.IssuedToken refresh = tokenProvider.generateRefreshToken(user);

        tokenProvider.revokeRefreshToken(refresh.token());

        verify(revocationService).revoke(anyString(), anyLong());
    }

    @Test
    void revokeRefreshToken_doesNothing_forAccessToken() {
        TokenProvider.IssuedToken access = tokenProvider.generateAccessToken(user);

        tokenProvider.revokeRefreshToken(access.token());

        verify(revocationService, org.mockito.Mockito.never()).revoke(any(), anyLong());
    }

    @Test
    void revokeRefreshToken_doesNotThrow_forGarbageInput() {
        tokenProvider.revokeRefreshToken("not-a-real-token");

        verify(revocationService, org.mockito.Mockito.never()).revoke(any(), anyLong());
    }

    @Test
    void generatedTokens_haveDistinctJtis() {
        TokenProvider.IssuedToken first = tokenProvider.generateRefreshToken(user);
        TokenProvider.IssuedToken second = tokenProvider.generateRefreshToken(user);

        assertThat(first.token()).isNotEqualTo(second.token());
    }
}
