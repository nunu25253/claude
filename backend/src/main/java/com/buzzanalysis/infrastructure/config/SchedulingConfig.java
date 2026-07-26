package com.buzzanalysis.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code @Scheduled} を有効化する。定期データ取得バッチ
 * （{@link com.buzzanalysis.infrastructure.scheduling.PeriodicSyncScheduler}）のcron実行に使用する。
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
