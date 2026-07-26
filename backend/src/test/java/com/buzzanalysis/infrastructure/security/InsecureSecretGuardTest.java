package com.buzzanalysis.infrastructure.security;

import com.buzzanalysis.infrastructure.storage.LocalStorageProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link InsecureSecretGuard} の単体テスト。
 * cookieSecure=false(開発環境)ではデフォルト値のままでも起動を許し、
 * cookieSecure=true(本番相当)ではデフォルト値を拒否することを検証する。
 */
class InsecureSecretGuardTest {

    @Test
    void allowsDefaultSecrets_whenCookieSecureIsFalse() {
        JwtProperties jwtProperties = new JwtProperties();
        LocalStorageProperties storageProperties = new LocalStorageProperties();
        InsecureSecretGuard guard = new InsecureSecretGuard(jwtProperties, storageProperties);

        assertThatCode(guard::validateSecretsAreOverriddenInProduction).doesNotThrowAnyException();
    }

    @Test
    void throws_whenCookieSecureIsTrue_andJwtSecretIsStillDefault() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setCookieSecure(true);
        LocalStorageProperties storageProperties = new LocalStorageProperties();
        storageProperties.setSigningSecret("a-properly-overridden-signing-secret");
        InsecureSecretGuard guard = new InsecureSecretGuard(jwtProperties, storageProperties);

        assertThatThrownBy(guard::validateSecretsAreOverriddenInProduction)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void throws_whenCookieSecureIsTrue_andSigningSecretIsStillDefault() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setCookieSecure(true);
        jwtProperties.setSecret("a-properly-overridden-jwt-secret");
        LocalStorageProperties storageProperties = new LocalStorageProperties();
        InsecureSecretGuard guard = new InsecureSecretGuard(jwtProperties, storageProperties);

        assertThatThrownBy(guard::validateSecretsAreOverriddenInProduction)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("STORAGE_LOCAL_SIGNING_SECRET");
    }

    @Test
    void allowsStartup_whenCookieSecureIsTrue_andBothSecretsAreOverridden() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setCookieSecure(true);
        jwtProperties.setSecret("a-properly-overridden-jwt-secret");
        LocalStorageProperties storageProperties = new LocalStorageProperties();
        storageProperties.setSigningSecret("a-properly-overridden-signing-secret");
        InsecureSecretGuard guard = new InsecureSecretGuard(jwtProperties, storageProperties);

        assertThatCode(guard::validateSecretsAreOverriddenInProduction).doesNotThrowAnyException();
    }
}
