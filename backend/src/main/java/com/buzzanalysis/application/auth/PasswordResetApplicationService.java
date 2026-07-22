package com.buzzanalysis.application.auth;

import com.buzzanalysis.domain.auth.PasswordResetToken;
import com.buzzanalysis.domain.auth.PasswordResetTokenRepository;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;

/**
 * パスワードリセット（依頼・確定）のユースケース。
 * パスワードを忘れたユーザーがアカウントに二度とアクセスできなくなる問題への対応。
 */
@Service
public class PasswordResetApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetApplicationService.class);
    private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final MailSenderPort mailSenderPort;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetApplicationService(UserRepository userRepository,
                                            PasswordResetTokenRepository passwordResetTokenRepository,
                                            PasswordEncoderPort passwordEncoderPort,
                                            MailSenderPort mailSenderPort) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.mailSenderPort = mailSenderPort;
    }

    /**
     * リセットメール送信を依頼する。メールアドレスが存在するかどうかで応答を変えると
     * アカウント列挙攻撃を許してしまうため、存在有無に関わらず常に正常終了する
     * （ログイン失敗時のメッセージ設計と同じ方針）。
     */
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            String rawToken = generateRawToken();
            PasswordResetToken token = PasswordResetToken.createNew(user.getId(), hash(rawToken), TOKEN_VALIDITY);
            passwordResetTokenRepository.save(token);
            mailSenderPort.sendPasswordResetEmail(user.getEmail(), rawToken);
        }, () -> log.debug("Password reset requested for unknown email (ignored to avoid account enumeration)"));
    }

    @Transactional
    public void confirmReset(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash(rawToken))
                .filter(PasswordResetToken::isValid)
                .orElseThrow(() -> new BusinessRuleViolationException("リンクが無効か有効期限が切れています。もう一度お試しください。"));

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessRuleViolationException("リンクが無効か有効期限が切れています。もう一度お試しください。"));

        user.changePassword(passwordEncoderPort.encode(newPassword));
        userRepository.save(user);

        token.markUsed();
        passwordResetTokenRepository.save(token);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
