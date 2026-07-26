package com.buzzanalysis.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** S3互換オブジェクトストレージ接続設定。application.ymlの {@code storage.s3.*} にバインドされる。MinIO等にも対応。 */
@ConfigurationProperties(prefix = "storage.s3")
public class S3Properties {

    /** MinIO等を使う場合のカスタムエンドポイント。AWS S3を直接使う場合は空のままでよい。 */
    private String endpoint = "";
    private String region = "ap-northeast-1";
    private String bucket = "buzz-analysis-reports";
    private String accessKey = "";
    private String secretKey = "";
    /** MinIO等のS3互換ストレージ利用時にtrueにするとpath-styleアクセスを強制する。 */
    private boolean pathStyleAccess = true;
    private long presignedUrlExpirationMinutes = 60;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public long getPresignedUrlExpirationMinutes() {
        return presignedUrlExpirationMinutes;
    }

    public void setPresignedUrlExpirationMinutes(long presignedUrlExpirationMinutes) {
        this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes;
    }

    public boolean hasCustomEndpoint() {
        return endpoint != null && !endpoint.isBlank();
    }

    public boolean hasStaticCredentials() {
        return accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank();
    }
}
