package com.buzzanalysis.application.auth;

import com.buzzanalysis.domain.auth.EmailVerificationToken;
import com.buzzanalysis.domain.auth.EmailVerificationTokenRepository;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 登録時のメールアドレス確認のユースケース。他人のメールアドレスでの登録(なりすまし)対策として、
 * 未確認アカウントはOpenAI呼び出しを伴う投稿分析(コスト発生エンドポイント)を制限する
 * ({@link com.buzzanalysis.application.post.PostAnalysisApplicationService}参照)。
 */
@Service
public class EmailVerificationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationApplicationService.class);
    private static final Duration TOKEN_VALIDITY = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final MailSenderPort mailSenderPort;

    public EmailVerificationApplicationService(UserRepository userRepository,
                                                EmailVerificationTokenRepository emailVerificationTokenRepository,
                                                MailSenderPort mailSenderPort) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.mailSenderPort = mailSenderPort;
    }

    /** 新規登録直後にAuthApplicationServiceから呼ばれる。確認メールの送信自体は登録処理を失敗させない。 */
    @Transactional
    public void sendVerificationEmail(User user) {
        String rawToken = TokenHasher.generateRawToken();
        EmailVerificationToken token = EmailVerificationToken.createNew(user.getId(), TokenHasher.hash(rawToken), TOKEN_VALIDITY);
        emailVerificationTokenRepository.save(token);
        mailSenderPort.sendEmailVerificationEmail(user.getEmail(), rawToken);
    }

    /**
     * 確認メールの再送を依頼する。メールアドレスが存在するか・既に確認済みかで応答を変えると
     * アカウント列挙攻撃を許してしまうため、常に正常終了する(パスワードリセットと同じ方針)。
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        userRepository.findByEmail(email)
                .filter(user -> !user.isEmailVerified())
                .ifPresentOrElse(this::sendVerificationEmail,
                        () -> log.debug("Verification email resend requested for unknown/already-verified email (ignored)"));
    }

    @Transactional
    public void confirmVerification(String rawToken) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(TokenHasher.hash(rawToken))
                .filter(EmailVerificationToken::isValid)
                .orElseThrow(() -> new BusinessRuleViolationException("リンクが無効か有効期限が切れています。もう一度お試しください。"));

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessRuleViolationException("リンクが無効か有効期限が切れています。もう一度お試しください。"));

        user.verifyEmail();
        userRepository.save(user);

        token.markUsed();
        emailVerificationTokenRepository.save(token);
    }
}
