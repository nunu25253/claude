package com.buzzanalysis.infrastructure.mail;

import com.buzzanalysis.application.auth.MailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * {@link JavaMailSender} を使ったパスワードリセットメール送信の実装。
 * ローカル開発ではdocker-compose上のMailHog（SMTP互換のテスト用メールキャッチャー）宛に送信し、
 * 本番ではSPRING_MAIL_HOST等の環境変数で実際のSMTPサーバーに差し替える想定。
 */
@Component
public class SmtpMailSenderAdapter implements MailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailSenderAdapter.class);

    private final JavaMailSender mailSender;
    private final PasswordResetMailProperties properties;

    public SmtpMailSenderAdapter(JavaMailSender mailSender, PasswordResetMailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String rawToken) {
        String resetLink = properties.getResetLinkBaseUrl() + "?token=" + rawToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromAddress());
        message.setTo(toEmail);
        message.setSubject("【SNS AIバズ分析】パスワード再設定のご案内");
        message.setText("""
                パスワード再設定のリクエストを受け付けました。

                以下のリンクから新しいパスワードを設定してください（1時間有効）。
                %s

                このメールに心当たりがない場合は、無視していただいて問題ありません。
                """.formatted(resetLink));
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // メール送信基盤（SMTPサーバー）が未設定/停止していても、パスワードリセットAPI自体は
            // 200を返す(アカウント列挙防止のため成否をレスポンスに反映しない)。ログにのみ記録する。
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }

    @Override
    public void sendEmailVerificationEmail(String toEmail, String rawToken) {
        String verificationLink = properties.getVerificationLinkBaseUrl() + "?token=" + rawToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromAddress());
        message.setTo(toEmail);
        message.setSubject("【SNS AIバズ分析】メールアドレスのご確認");
        message.setText("""
                ご登録ありがとうございます。

                以下のリンクからメールアドレスの確認を完了してください（24時間有効）。
                %s

                このメールに心当たりがない場合は、無視していただいて問題ありません。
                """.formatted(verificationLink));
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // 登録処理自体は失敗させない(メール送信基盤の一時的な不調でユーザー登録をブロックしない)。
            log.error("Failed to send email verification mail to {}", toEmail, e);
        }
    }
}
