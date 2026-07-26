package com.buzzanalysis.infrastructure.quota;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ユーザー単位の日次API利用上限設定。application.ymlの {@code app.quota.*} にバインドされる。
 * 有料のOpenAI呼び出しを伴うエンドポイントを無制限に叩けるとコストが青天井になるため導入する。
 * 課金プラン(改善計画No.11)によってFREE/PROで上限値を分ける。
 */
@ConfigurationProperties(prefix = "app.quota")
public class UsageQuotaProperties {

    private int dailyOpenAiCallsFree = 50;
    private int dailyOpenAiCallsPro = 500;

    public int getDailyOpenAiCallsFree() {
        return dailyOpenAiCallsFree;
    }

    public void setDailyOpenAiCallsFree(int dailyOpenAiCallsFree) {
        this.dailyOpenAiCallsFree = dailyOpenAiCallsFree;
    }

    public int getDailyOpenAiCallsPro() {
        return dailyOpenAiCallsPro;
    }

    public void setDailyOpenAiCallsPro(int dailyOpenAiCallsPro) {
        this.dailyOpenAiCallsPro = dailyOpenAiCallsPro;
    }
}
