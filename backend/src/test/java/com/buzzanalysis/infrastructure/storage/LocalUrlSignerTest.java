package com.buzzanalysis.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/** {@link LocalUrlSigner} の署名生成・検証ロジックの単体テスト。 */
class LocalUrlSignerTest {

    private LocalUrlSigner signer;

    @BeforeEach
    void setUp() {
        LocalStorageProperties properties = new LocalStorageProperties();
        properties.setSigningSecret("test-signing-secret-0123456789");
        signer = new LocalUrlSigner(properties);
    }

    @Test
    void isValid_true_forCorrectSignatureWithinExpiration() {
        String key = "reports/abc/report.html";
        long expires = Instant.now().plusSeconds(3600).getEpochSecond();

        String signature = signer.sign(key, expires);

        assertThat(signer.isValid(key, expires, signature)).isTrue();
    }

    @Test
    void isValid_false_whenExpired() {
        String key = "reports/abc/report.html";
        long expiredAt = Instant.now().minusSeconds(10).getEpochSecond();

        String signature = signer.sign(key, expiredAt);

        assertThat(signer.isValid(key, expiredAt, signature)).isFalse();
    }

    @Test
    void isValid_false_whenSignatureTampered() {
        String key = "reports/abc/report.html";
        long expires = Instant.now().plusSeconds(3600).getEpochSecond();
        String signature = signer.sign(key, expires);

        assertThat(signer.isValid(key, expires, signature + "x")).isFalse();
    }

    @Test
    void isValid_false_whenKeyDiffersFromSignedKey() {
        String signedKey = "reports/abc/report.html";
        long expires = Instant.now().plusSeconds(3600).getEpochSecond();
        String signature = signer.sign(signedKey, expires);

        assertThat(signer.isValid("reports/other/report.html", expires, signature)).isFalse();
    }

    @Test
    void isValid_false_whenSignatureBlank() {
        assertThat(signer.isValid("k", Instant.now().plusSeconds(60).getEpochSecond(), "")).isFalse();
        assertThat(signer.isValid("k", Instant.now().plusSeconds(60).getEpochSecond(), null)).isFalse();
    }
}
