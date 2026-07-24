package com.buzzanalysis.application.auth;

import com.buzzanalysis.application.auth.dto.AuthResult;
import com.buzzanalysis.application.auth.dto.LoginCommand;
import com.buzzanalysis.application.auth.dto.RegisterCommand;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 認証（登録/ログイン/トークンリフレッシュ）に関するアプリケーションサービス。
 * ユースケースの調整のみを行い、実際のパスワードハッシュ化やJWT発行はポート経由でinfrastructure層に委譲する。
 */
@Service
public class AuthApplicationService {

    /** Bot対策検証失敗時にフロントエンドが判別するためのerrorCode。 */
    public static final String CAPTCHA_VERIFICATION_FAILED_CODE = "CAPTCHA_VERIFICATION_FAILED";

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenProvider tokenProvider;
    private final EmailVerificationApplicationService emailVerificationApplicationService;
    private final CaptchaVerificationPort captchaVerificationPort;

    public AuthApplicationService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort,
                                   TokenProvider tokenProvider,
                                   EmailVerificationApplicationService emailVerificationApplicationService,
                                   CaptchaVerificationPort captchaVerificationPort) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenProvider = tokenProvider;
        this.emailVerificationApplicationService = emailVerificationApplicationService;
        this.captchaVerificationPort = captchaVerificationPort;
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
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new BusinessRuleViolationException("Invalid email or password"));
        if (!passwordEncoderPort.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new BusinessRuleViolationException("Invalid email or password");
        }
        return issueTokens(user);
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
