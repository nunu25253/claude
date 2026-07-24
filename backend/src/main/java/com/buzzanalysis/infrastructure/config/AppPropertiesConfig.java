package com.buzzanalysis.infrastructure.config;

import com.buzzanalysis.infrastructure.external.openai.OpenAiProperties;
import com.buzzanalysis.infrastructure.external.platform.instagram.InstagramApiProperties;
import com.buzzanalysis.infrastructure.external.platform.tiktok.TikTokApiProperties;
import com.buzzanalysis.infrastructure.external.platform.x.XApiProperties;
import com.buzzanalysis.infrastructure.mail.PasswordResetMailProperties;
import com.buzzanalysis.infrastructure.payment.GmoPaymentProperties;
import com.buzzanalysis.infrastructure.quota.UsageQuotaProperties;
import com.buzzanalysis.infrastructure.scheduling.BatchAlertProperties;
import com.buzzanalysis.infrastructure.scheduling.BatchSyncProperties;
import com.buzzanalysis.infrastructure.scheduling.BatchTrendProperties;
import com.buzzanalysis.infrastructure.scheduling.BillingRenewalProperties;
import com.buzzanalysis.infrastructure.security.AccountLockoutProperties;
import com.buzzanalysis.infrastructure.security.CaptchaProperties;
import com.buzzanalysis.infrastructure.security.CorsProperties;
import com.buzzanalysis.infrastructure.security.JwtProperties;
import com.buzzanalysis.infrastructure.security.RateLimitProperties;
import com.buzzanalysis.infrastructure.storage.LocalStorageProperties;
import com.buzzanalysis.infrastructure.storage.S3Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** {@code @ConfigurationProperties} クラス群をまとめて有効化する。 */
@Configuration
@EnableConfigurationProperties({
        InstagramApiProperties.class,
        TikTokApiProperties.class,
        XApiProperties.class,
        OpenAiProperties.class,
        JwtProperties.class,
        S3Properties.class,
        LocalStorageProperties.class,
        BatchSyncProperties.class,
        BatchTrendProperties.class,
        BatchAlertProperties.class,
        CorsProperties.class,
        UsageQuotaProperties.class,
        RateLimitProperties.class,
        CaptchaProperties.class,
        AccountLockoutProperties.class,
        PasswordResetMailProperties.class,
        GmoPaymentProperties.class,
        BillingRenewalProperties.class
})
public class AppPropertiesConfig {
}
