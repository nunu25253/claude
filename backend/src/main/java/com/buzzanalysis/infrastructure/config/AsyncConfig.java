package com.buzzanalysis.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * {@code @Async} を有効化する。Observerパターンのイベントリスナー
 * （{@link com.buzzanalysis.application.event.AnalysisCompletedEventListener}）を
 * リクエストスレッドから切り離して非同期実行するために使用する。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * メール送信専用の有界スレッドプール。SMTP送信(
     * {@link com.buzzanalysis.infrastructure.mail.SmtpMailSenderAdapter}
     * )はリクエストスレッドをブロックしてはならないため{@code @Async}で切り離すが、
     * 既定の{@code SimpleAsyncTaskExecutor}(呼び出しごとに無制限にスレッドを生成)は
     * SMTP障害時にスレッドが際限なく増え続けるおそれがあるため、専用の有界プールを用意する。
     */
    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mail-async-");
        executor.initialize();
        return executor;
    }
}
