package com.buzzanalysis.infrastructure.security;

import com.buzzanalysis.infrastructure.storage.LocalStorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * JWT署名鍵・署名付きURL鍵がコード同梱のデフォルト値のまま本番相当の設定で起動されるのを防ぐ
 * fail-fastガード。
 *
 * <p>このプロジェクトには専用の"prod"プロファイルが存在せず(ローカル開発は無指定、
 * docker-compose環境は"docker"プロファイルだがこれも開発用)、プロファイル名だけでは
 * 「本番かどうか」を機械的に判定できない。そこで{@link JwtProperties#isCookieSecure()}
 * (本番HTTPS環境でのみtrueにする、と既存のフィールドコメントで既に定義されている規約)を
 * 「これは本番相当のデプロイである」という信頼できるシグナルとして利用し、その場合のみ
 * デフォルト値のままの秘密鍵を拒否する。開発環境(cookieSecure=false)では従来通り
 * デフォルト値のまま起動できる。</p>
 */
@Component
public class InsecureSecretGuard {

    static final String INSECURE_DEFAULT = "change-this-secret-in-production-please-0123456789abcdef";

    private final JwtProperties jwtProperties;
    private final LocalStorageProperties storageProperties;

    public InsecureSecretGuard(JwtProperties jwtProperties, LocalStorageProperties storageProperties) {
        this.jwtProperties = jwtProperties;
        this.storageProperties = storageProperties;
    }

    @PostConstruct
    void validateSecretsAreOverriddenInProduction() {
        if (!jwtProperties.isCookieSecure()) {
            return;
        }
        if (INSECURE_DEFAULT.equals(jwtProperties.getSecret())) {
            throw new IllegalStateException(
                    "JWT_COOKIE_SECURE=true (production-like deployment) but JWT_SECRET is still the "
                            + "insecure default value. Set JWT_SECRET to a unique random value before starting.");
        }
        if (INSECURE_DEFAULT.equals(storageProperties.getSigningSecret())) {
            throw new IllegalStateException(
                    "JWT_COOKIE_SECURE=true (production-like deployment) but STORAGE_LOCAL_SIGNING_SECRET is "
                            + "still the insecure default value. Set STORAGE_LOCAL_SIGNING_SECRET to a unique "
                            + "random value before starting.");
        }
    }
}
