package com.buzzanalysis.infrastructure.mail;

import com.buzzanalysis.application.auth.MailSenderPort;
import com.buzzanalysis.application.auth.dto.GenreScoreSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link JavaMailSender} を使ったパスワードリセットメール送信の実装。
 * ローカル開発ではdocker-compose上のMailHog（SMTP互換のテスト用メールキャッチャー）宛に送信し、
 * 本番ではSPRING_MAIL_HOST等の環境変数で実際のSMTPサーバーに差し替える想定。
 *
 * <p>各メソッドは{@code @Async("mailTaskExecutor")}でリクエストスレッドから切り離して実行する。
 * SMTP送信は同期呼び出しのままだと接続先の遅延・障害がそのまま新規登録/パスワードリセット等の
 * APIレスポンスをブロックしてしまう(タイムアウト設定も{@code application.yml}側で別途行う)。</p>
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
    @Async("mailTaskExecutor")
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
    @Async("mailTaskExecutor")
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

    @Override
    @Async("mailTaskExecutor")
    public void sendThresholdAlertEmail(String toEmail, String postCaption, double currentScore, double threshold) {
        String caption = (postCaption == null || postCaption.isBlank()) ? "(キャプションなし)" : postCaption;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromAddress());
        message.setTo(toEmail);
        message.setSubject("【SNS AIバズ分析】設定したしきい値を超えました");
        message.setText("""
                保存済み分析に設定したBuzzScoreのしきい値を超えました。

                投稿: %s
                現在のBuzzScore: %.1f (しきい値: %.1f)

                以下から詳細をご確認ください。
                %s
                """.formatted(caption, currentScore, threshold, properties.getSavedAnalysesUrl()));
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // 通知送信の失敗でバッチ処理全体を止めない(他の保存済み分析のチェックは継続する)。
            log.error("Failed to send threshold alert mail to {}", toEmail, e);
        }
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendWeeklyDigestEmail(String toEmail, List<GenreScoreSummary> topGenres, double thisWeekAverageScore,
                                       Double lastWeekAverageScore, int savedAnalysisCount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromAddress());
        message.setTo(toEmail);
        message.setSubject("【SNS AIバズ分析】今週のAIダイジェスト");
        message.setText("""
                今週のAIダイジェストをお届けします。

                今週のあなたの平均BuzzScore: %.1f%s
                保存済み分析: %d件

                %s
                以下から詳細をご確認ください。
                %s
                """.formatted(
                thisWeekAverageScore,
                formatWeekOverWeek(thisWeekAverageScore, lastWeekAverageScore),
                savedAnalysisCount,
                formatTopGenres(topGenres),
                properties.getSavedAnalysesUrl()));
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // 通知送信の失敗でバッチ処理全体を止めない(他のユーザーへの送信は継続する)。
            log.error("Failed to send weekly digest mail to {}", toEmail, e);
        }
    }

    private String formatWeekOverWeek(double thisWeekAverageScore, Double lastWeekAverageScore) {
        if (lastWeekAverageScore == null) {
            return "";
        }
        double delta = thisWeekAverageScore - lastWeekAverageScore;
        String sign = delta >= 0 ? "+" : "";
        return " (先週比 %s%.1f)".formatted(sign, delta);
    }

    private String formatTopGenres(List<GenreScoreSummary> topGenres) {
        if (topGenres.isEmpty()) {
            return "";
        }
        String lines = topGenres.stream()
                .map(g -> "  ・%s (平均BuzzScore %.1f)".formatted(g.genre(), g.averageScore()))
                .collect(Collectors.joining("\n"));
        return """
                今週プラットフォーム全体で好調なジャンル:
                %s

                """.formatted(lines);
    }
}
