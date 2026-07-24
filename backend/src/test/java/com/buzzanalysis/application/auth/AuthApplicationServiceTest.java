package com.buzzanalysis.application.auth;

import com.buzzanalysis.application.auth.dto.AuthResult;
import com.buzzanalysis.application.auth.dto.LoginCommand;
import com.buzzanalysis.application.auth.dto.RegisterCommand;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import com.buzzanalysis.infrastructure.security.AccountLockoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link AuthApplicationService} のMockitoによる単体テスト。
 * 依存する UserRepository / PasswordEncoderPort / TokenProvider はすべてモック化する。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthApplicationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoderPort passwordEncoderPort;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private EmailVerificationApplicationService emailVerificationApplicationService;
    @Mock
    private CaptchaVerificationPort captchaVerificationPort;
    @Mock
    private AccountLockoutService accountLockoutService;

    private AuthApplicationService service;

    @BeforeEach
    void setUp() {
        service = new AuthApplicationService(userRepository, passwordEncoderPort, tokenProvider,
                emailVerificationApplicationService, captchaVerificationPort, accountLockoutService);
        when(captchaVerificationPort.verify(any(), any())).thenReturn(true);
        when(accountLockoutService.isLocked(anyString())).thenReturn(false);
    }

    @Test
    void register_savesNewUserAndIssuesTokens() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoderPort.encode("Password123!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn(new TokenProvider.IssuedToken("access-token", 1800));
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn(new TokenProvider.IssuedToken("refresh-token", 1209600));

        AuthResult result = service.register(
                new RegisterCommand("new@example.com", "Password123!", "New User", "captcha-token", "127.0.0.1"));

        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUserCaptor.capture());
        assertThat(savedUserCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUserCaptor.getValue().isEmailVerified()).isFalse();
        verify(emailVerificationApplicationService).sendVerificationEmail(savedUserCaptor.getValue());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterCommand("existing@example.com", "Password123!", "User", "captcha-token", "127.0.0.1")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void register_throwsBusinessRuleViolation_withCaptchaErrorCode_whenCaptchaVerificationFails() {
        when(captchaVerificationPort.verify("bad-token", "127.0.0.1")).thenReturn(false);

        assertThatThrownBy(() -> service.register(
                new RegisterCommand("new@example.com", "Password123!", "New User", "bad-token", "127.0.0.1")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(e -> assertThat(((BusinessRuleViolationException) e).getErrorCode())
                        .isEqualTo(AuthApplicationService.CAPTCHA_VERIFICATION_FAILED_CODE));
        verifyNoInteractions(userRepository);
    }

    @Test
    void login_rejectsInvalidPassword() {
        User existingUser = new User(UUID.randomUUID(), "user@example.com", "hashed", "User", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoderPort.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginCommand("user@example.com", "wrong-password")))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(accountLockoutService).recordFailure("user@example.com");
    }

    @Test
    void login_recordsFailure_whenEmailDoesNotExist() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginCommand("unknown@example.com", "any-password")))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(accountLockoutService).recordFailure("unknown@example.com");
    }

    @Test
    void login_succeedsWithCorrectPassword() {
        User existingUser = new User(UUID.randomUUID(), "user@example.com", "hashed", "User", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoderPort.matches("correct-password", "hashed")).thenReturn(true);
        when(tokenProvider.generateAccessToken(existingUser)).thenReturn(new TokenProvider.IssuedToken("access-token", 1800));
        when(tokenProvider.generateRefreshToken(existingUser)).thenReturn(new TokenProvider.IssuedToken("refresh-token", 1209600));

        AuthResult result = service.login(new LoginCommand("user@example.com", "correct-password"));

        assertThat(result.userId()).isEqualTo(existingUser.getId());
        verify(accountLockoutService).recordSuccess("user@example.com");
    }

    @Test
    void login_throwsBusinessRuleViolation_withAccountLockedErrorCode_whenAccountIsLocked() {
        when(accountLockoutService.isLocked("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginCommand("user@example.com", "any-password")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(e -> assertThat(((BusinessRuleViolationException) e).getErrorCode())
                        .isEqualTo(AccountLockoutService.ACCOUNT_LOCKED_ERROR_CODE));
        verifyNoInteractions(userRepository, passwordEncoderPort);
    }

    @Test
    void logout_revokesRefreshToken_whenPresent() {
        service.logout("some-refresh-token");

        verify(tokenProvider).revokeRefreshToken("some-refresh-token");
    }

    @Test
    void logout_doesNothing_whenRefreshTokenIsNullOrBlank() {
        service.logout(null);
        service.logout("");
        service.logout("   ");

        verify(tokenProvider, org.mockito.Mockito.never()).revokeRefreshToken(anyString());
    }
}
