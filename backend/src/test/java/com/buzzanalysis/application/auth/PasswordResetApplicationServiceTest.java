package com.buzzanalysis.application.auth;

import com.buzzanalysis.domain.auth.PasswordResetToken;
import com.buzzanalysis.domain.auth.PasswordResetTokenRepository;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PasswordResetApplicationService} の単体テスト。
 * パスワードを忘れたユーザーが永久にログイン不能になる問題への対応として実装したフローの検証。
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetApplicationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoderPort passwordEncoderPort;
    @Mock
    private MailSenderPort mailSenderPort;

    private PasswordResetApplicationService service;
    private User existingUser;

    @BeforeEach
    void setUp() {
        service = new PasswordResetApplicationService(userRepository, passwordResetTokenRepository,
                passwordEncoderPort, mailSenderPort);
        existingUser = new User(UUID.randomUUID(), "user@example.com", "old-hash", "User",
                Role.USER, OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void requestReset_savesTokenAndSendsMail_whenEmailExists() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordResetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.requestReset("user@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUserId()).isEqualTo(existingUser.getId());
        assertThat(tokenCaptor.getValue().isValid()).isTrue();

        verify(mailSenderPort).sendPasswordResetEmail(eq("user@example.com"), any());
    }

    @Test
    void requestReset_doesNothingSilently_whenEmailDoesNotExist() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        service.requestReset("unknown@example.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(mailSenderPort, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void confirmReset_updatesPasswordAndMarksTokenUsed_whenTokenIsValid() {
        String rawToken = "raw-token-value";
        PasswordResetToken token = PasswordResetToken.createNew(existingUser.getId(), sha256(rawToken), Duration.ofHours(1));
        when(passwordResetTokenRepository.findByTokenHash(sha256(rawToken))).thenReturn(Optional.of(token));
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(passwordEncoderPort.encode("newPassword123")).thenReturn("new-hash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordResetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.confirmReset(rawToken, "newPassword123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("new-hash");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().isValid()).isFalse();
    }

    @Test
    void confirmReset_throws_whenTokenDoesNotExist() {
        when(passwordResetTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmReset("nonexistent-token", "newPassword123"))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmReset_throws_whenTokenAlreadyUsed() {
        String rawToken = "raw-token-value";
        PasswordResetToken token = PasswordResetToken.createNew(existingUser.getId(), sha256(rawToken), Duration.ofHours(1));
        token.markUsed();
        when(passwordResetTokenRepository.findByTokenHash(sha256(rawToken))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmReset(rawToken, "newPassword123"))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void confirmReset_throws_whenTokenExpired() {
        String rawToken = "raw-token-value";
        PasswordResetToken token = PasswordResetToken.createNew(existingUser.getId(), sha256(rawToken), Duration.ofHours(-1));
        when(passwordResetTokenRepository.findByTokenHash(sha256(rawToken))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirmReset(rawToken, "newPassword123"))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(userRepository, never()).save(any());
    }

    private static String sha256(String raw) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
