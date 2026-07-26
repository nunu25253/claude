package com.buzzanalysis.application.auth;

import com.buzzanalysis.application.auth.dto.GenreScoreSummary;

import java.util.List;

/**
 * パスワードリセットメールの送信を行うポート。実装（SMTP等）はinfrastructure層に置く。
 */
public interface MailSenderPort {

    /**
     * パスワードリセット用のリンクを含むメールを送信する。
     *
     * @param toEmail  送信先メールアドレス
     * @param rawToken リセットリンクに埋め込む生トークン（ハッシュ化前）
     */
    void sendPasswordResetEmail(String toEmail, String rawToken);

    /**
     * メールアドレス確認用のリンクを含むメールを送信する。
     *
     * @param toEmail  送信先メールアドレス
     * @param rawToken 確認リンクに埋め込む生トークン（ハッシュ化前）
     */
    void sendEmailVerificationEmail(String toEmail, String rawToken);

    /**
     * 保存済み分析のBuzzScoreしきい値超過を知らせるメールを送信する。
     *
     * @param toEmail        送信先メールアドレス
     * @param postCaption    対象投稿のキャプション（表示用、空の場合あり）
     * @param currentScore   現在のBuzzScore
     * @param threshold      設定されていたしきい値
     */
    void sendThresholdAlertEmail(String toEmail, String postCaption, double currentScore, double threshold);

    /**
     * 週次AIダイジェストメールを送信する。直近1週間に活動(BuzzScore再計算)があったユーザーにのみ送る想定
     * (呼び出し側で判定済み)。プラットフォーム全体の勝ちジャンルと、本人の週次スコア推移を知らせる。
     *
     * @param toEmail             送信先メールアドレス
     * @param topGenres           プラットフォーム全体の平均BuzzScore上位ジャンル(空の場合あり)
     * @param thisWeekAverageScore 今週の本人の平均BuzzScore
     * @param lastWeekAverageScore 先週の本人の平均BuzzScore(データがない場合null)
     * @param savedAnalysisCount  本人の保存済み分析の総件数
     */
    void sendWeeklyDigestEmail(String toEmail, List<GenreScoreSummary> topGenres, double thisWeekAverageScore,
                                Double lastWeekAverageScore, int savedAnalysisCount);
}
