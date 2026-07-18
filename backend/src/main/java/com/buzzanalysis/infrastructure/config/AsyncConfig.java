package com.buzzanalysis.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * {@code @Async} を有効化する。Observerパターンのイベントリスナー
 * （{@link com.buzzanalysis.application.event.AnalysisCompletedEventListener}）を
 * リクエストスレッドから切り離して非同期実行するために使用する。
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
