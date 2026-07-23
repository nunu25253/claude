package com.buzzanalysis.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ローカルファイルシステム版{@link com.buzzanalysis.application.report.StoragePort}の設定。
 * application.ymlの {@code storage.local.*} にバインドされる。
 */
@ConfigurationProperties(prefix = "storage.local")
public class LocalStorageProperties {

    private String baseDir = "./data/reports";
    private long presignedUrlExpirationMinutes = 60;
    /** URL署名用のHMAC鍵。JWT_SECRET同様、本番では必ず環境変数で上書きすること。 */
    private String signingSecret = "change-this-secret-in-production-please-0123456789abcdef";

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public long getPresignedUrlExpirationMinutes() {
        return presignedUrlExpirationMinutes;
    }

    public void setPresignedUrlExpirationMinutes(long presignedUrlExpirationMinutes) {
        this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes;
    }

    public String getSigningSecret() {
        return signingSecret;
    }

    public void setSigningSecret(String signingSecret) {
        this.signingSecret = signingSecret;
    }
}
