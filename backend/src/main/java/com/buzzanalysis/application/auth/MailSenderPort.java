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
}
