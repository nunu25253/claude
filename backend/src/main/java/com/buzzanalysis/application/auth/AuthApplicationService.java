package com.buzzanalysis.application.auth;

import com.buzzanalysis.application.auth.dto.AuthResult;
import com.buzzanalysis.application.auth.dto.LoginCommand;
import com.buzzanalysis.application.auth.dto.RegisterCommand;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import com.buzzanalysis.infrastructure.security.AccountLockoutService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 認証（登録/ログイン/トークンリフレッシュ）に関するアプリケーションサービス。
 * ユースケースの調整のみを行い、実際のパスワードハッシュ化やJWT発行はポート経由でinfrastructure層に委譲する。
 */
@Service
public class AuthApplicationService {

    /** Bot対策検証失敗時にフロントエンドが判別するためのerrorCode。 */
    public static final String CAPTCHA_VERIFICATION_FAILED_CODE = "CAPTCHA_VERIFICATION_FAILED";

    /** アカウント削除時のパスワード確認失敗時にフロントエンドが判別するためのerrorCode。 */
    public static final String DELETE_ACCOUNT_INVALID_PASSWORD_CODE = "DELETE_ACCOUNT_INVALID_PASSWORD";

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenProvider tokenProvider;
    private final EmailVerificationApplicationService emailVerificationApplicationService;
    private final CaptchaVerificationPort captchaVerificationPort;
    private final AccountLockoutService accountLockoutService;

    public AuthApplicationService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort,
                                   TokenProvider tokenProvider,
                                   EmailVerificationApplicationService emailVerificationApplicationService,
                                   CaptchaVerificationPort captchaVerificationPort,
                                   AccountLockoutService accountLockoutService) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenProvider = tokenProvider;
        this.emailVerificationApplicationService = emailVerificationApplicationService;
        this.captchaVerificationPort = captchaVerificationPort;
        this.accountLockoutService = accountLockoutService;
    }

    @Transactional
    public AuthResult register(RegisterCommand command) {
        if (!captchaVerificationPort.verify(command.captchaToken(), command.remoteIp())) {
            throw new BusinessRuleViolationException("ボット確認に失敗しました。もう一度お試しください。",
                    CAPTCHA_VERIFICATION_FAILED_CODE);
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessRuleViolationException("Email is already registered: " + command.email());
        }
        String hashed = passwordEncoderPort.encode(command.rawPassword());
        User user = User.createNew(command.email(), hashed, command.displayName());
        User saved = userRepository.save(user);
        emailVerificationApplicationService.sendVerificationEmail(saved);
        return issueTokens(saved);
    }

    @Transactional(readOnly = true)
    public AuthResult login(LoginCommand command) {
        if (accountLockoutService.isLocked(command.email())) {
            throw new BusinessRuleViolationException(
                    "ログイン試行回数が多すぎるため一時的にロックされています。しばらくしてから再度お試しください。",
                    AccountLockoutService.ACCOUNT_LOCKED_ERROR_CODE);
        }

        Optional<User> maybeUser = userRepository.findByEmail(command.email());
        boolean credentialsValid = maybeUser.isPresent()
                && passwordEncoderPort.matches(command.rawPassword(), maybeUser.get().getPasswordHash());
        if (!credentialsValid) {
            accountLockoutService.recordFailure(command.email());
            throw new BusinessRuleViolationException("Invalid email or password");
        }

        accountLockoutService.recordSuccess(command.email());
        return issueTokens(maybeUser.get());
    }

    @Transactional(readOnly = true)
    public AuthResult refresh(String refreshToken) {
        UUID userId = tokenProvider.validateRefreshTokenAndGetUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.of("User", userId));
        return issueTokens(user);
    }

    /**
     * リフレッシュトークンを失効させる(ログアウト)。トークンが無い/既に無効な場合も
     * べき等に成功として扱う(二重ログアウトでエラーにする必要は無いため)。
     */
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenProvider.revokeRefreshToken(refreshToken);
        }
    }

    /**
     * アカウントを削除する(退会)。誤操作・第三者による不正操作を防ぐため現在のパスワードの
     * 再確認を必須とする。関連データ(保存済み分析・チーム所属・購読・各種トークン等)は
     * DBのON DELETE CASCADE/SET NULL設定により整合的に削除・匿名化される
     * (V24__account_deletion_cascades.sql参照)。削除後は現在のセッションのリフレッシュ
     * トークンも失効させる。
     */
    @Transactional
    public void deleteAccount(UUID userId, String currentPassword, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.of("User", userId));
        if (!passwordEncoderPort.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessRuleViolationException("パスワードが正しくありません。",
                    DELETE_ACCOUNT_INVALID_PASSWORD_CODE);
        }
        userRepository.deleteById(userId);
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenProvider.revokeRefreshToken(refreshToken);
        }
    }

    private AuthResult issueTokens(User user) {
        TokenProvider.IssuedToken access = tokenProvider.generateAccessToken(user);
        TokenProvider.IssuedToken refresh = tokenProvider.generateRefreshToken(user);
        return new AuthResult(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.isEmailVerified(),
                access.token(),
                access.expiresInSeconds(),
                refresh.token(),
                refresh.expiresInSeconds()
        );
    }
}
