package com.buzzanalysis.application.auth;

import com.buzzanalysis.domain.auth.EmailVerificationToken;
import com.buzzanalysis.domain.auth.EmailVerificationTokenRepository;
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
 * {@link EmailVerificationApplicationService} の単体テスト。
 * 他人のメールアドレスでの登録(なりすまし)対策として実装した、登録時のメール確認フローの検証。
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationApplicationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock
    private MailSenderPort mailSenderPort;

    private EmailVerificationApplicationService service;
    private User unverifiedUser;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationApplicationService(userRepository, emailVerificationTokenRepository, mailSenderPort);
        unverifiedUser = new User(UUID.randomUUID(), "user@example.com", "hash", "User", Role.USER, false,
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void sendVerificationEmail_savesTokenAndSendsMail() {
        when(emailVerificationTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.sendVerificationEmail(unverifiedUser);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailVerificationTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUserId()).isEqualTo(unverifiedUser.getId());
        assertThat(tokenCaptor.getValue().isValid()).isTrue();
        verify(mailSenderPort).sendEmailVerificationEmail(eq("user@example.com"), any());
    }

    @Test
    void resendVerificationEmail_doesNothing_whenAlreadyVerified() {
        User verifiedUser = new User(UUID.randomUUID(), "verified@example.com", "hash", "User", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
        when(userRepository.findByEmail("verified@example.com")).thenReturn(Optional.of(verifiedUser));

        service.resendVerificationEmail("verified@example.com");

        verify(emailVerificationTokenRepository, never()).save(any());
        verify(mailSenderPort, never()).sendEmailVerificationEmail(any(), any());
    }

    @Test
    void confirmVerification_marksUserVerifiedAndTokenUsed_whenTokenIsValid() {
        String rawToken = "raw-token-value";
        EmailVerificationToken token = EmailVerificationToken.createNew(unverifiedUser.getId(), sha256(rawToken), Duration.ofHours(24));
        when(emailVerificationTokenRepository.findByTokenHash(sha256(rawToken))).thenReturn(Optional.of(token));
        when(userRepository.findById(unverifiedUser.getId())).thenReturn(Optional.of(unverifiedUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(emailVerificationTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.confirmVerification(rawToken);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().isEmailVerified()).isTrue();

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailVerificationTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().isValid()).isFalse();
    }

    @Test
    void confirmVerification_throws_whenTokenDoesNotExist() {
        when(emailVerificationTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmVerification("nonexistent-token"))
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
