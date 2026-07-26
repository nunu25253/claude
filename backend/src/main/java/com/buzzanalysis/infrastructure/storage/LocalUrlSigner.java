package com.buzzanalysis.infrastructure.storage;

import com.buzzanalysis.domain.common.exception.ExternalApiException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

/**
 * ローカルストレージのレポートファイルURLをHMAC-SHA256で署名する。S3の署名付きURL(有効期限あり)と
 * 同じ安全性の考え方を、{@code storage.provider=local} でも成立させるために使用する
 * ({@link LocalFileStorageService#generateAccessUrl}は恒久的に有効な生パスを返してはならない)。
 */
@Component
public class LocalUrlSigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final LocalStorageProperties properties;

    public LocalUrlSigner(LocalStorageProperties properties) {
        this.properties = properties;
    }

    /** 指定キー・有効期限(epoch秒)に対する署名を生成する。 */
    public String sign(String key, long expiresEpochSeconds) {
        return computeSignature(key, expiresEpochSeconds);
    }

    /**
     * 署名・有効期限を検証する。改ざん検知のため定数時間比較({@link MessageDigest#isEqual})を用いる。
     */
    public boolean isValid(String key, long expiresEpochSeconds, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        if (Instant.now().getEpochSecond() > expiresEpochSeconds) {
            return false;
        }
        String expected = computeSignature(key, expiresEpochSeconds);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }

    private String computeSignature(String key, long expiresEpochSeconds) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.getSigningSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] raw = mac.doFinal((key + ":" + expiresEpochSeconds).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            throw new ExternalApiException("Failed to sign local storage URL", e);
        }
    }
}
