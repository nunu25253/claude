package com.buzzanalysis.application.auth;

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
}
