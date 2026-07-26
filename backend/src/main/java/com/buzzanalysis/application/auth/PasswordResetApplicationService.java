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

import java.time.Duration;

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
            String rawToken = TokenHasher.generateRawToken();
            PasswordResetToken token = PasswordResetToken.createNew(user.getId(), TokenHasher.hash(rawToken), TOKEN_VALIDITY);
            passwordResetTokenRepository.save(token);
            mailSenderPort.sendPasswordResetEmail(user.getEmail(), rawToken);
        }, () -> log.debug("Password reset requested for unknown email (ignored to avoid account enumeration)"));
    }

    @Transactional
    public void confirmReset(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(TokenHasher.hash(rawToken))
                .filter(PasswordResetToken::isValid)
                .orElseThrow(() -> new BusinessRuleViolationException("リンクが無効か有効期限が切れています。もう一度お試しください。"));

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessRuleViolationException("リンクが無効か有効期限が切れています。もう一度お試しください。"));

        user.changePassword(passwordEncoderPort.encode(newPassword));
        userRepository.save(user);

        token.markUsed();
        passwordResetTokenRepository.save(token);
    }
}
