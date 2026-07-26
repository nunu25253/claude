package com.buzzanalysis.domain.notification;

/**
 * ユーザーが設定したSlack Incoming Webhookへメッセージを送信するポート。
 * メール到達率・開封率の低さを補う代替チャネルとして、通知イベント発生時に呼び出す。
 */
public interface SlackNotifierPort {

    void sendMessage(String webhookUrl, String text);
}
