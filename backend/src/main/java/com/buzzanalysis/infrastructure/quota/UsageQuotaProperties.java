package com.buzzanalysis.infrastructure.quota;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ユーザー単位の日次API利用上限設定。application.ymlの {@code app.quota.*} にバインドされる。
 * 有料のOpenAI呼び出しを伴うエンドポイントを無制限に叩けるとコストが青天井になるため導入する。
 */
@ConfigurationProperties(prefix = "app.quota")
public class UsageQuotaProperties {

    private int dailyOpenAiCalls = 50;

    public int getDailyOpenAiCalls() {
        return dailyOpenAiCalls;
    }

    public void setDailyOpenAiCalls(int dailyOpenAiCalls) {
        this.dailyOpenAiCalls = dailyOpenAiCalls;
    }
}
