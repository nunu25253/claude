package com.buzzanalysis.infrastructure.scheduling;

import com.buzzanalysis.application.trend.TrendAnalysisApplicationService;
import com.buzzanalysis.application.trend.dto.TrendAnalysisRequest;
import com.buzzanalysis.application.trend.dto.TrendReportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * トレンド分析（Phase15）の日次自動実行を担う。{@link PeriodicSyncScheduler}と同じパターンで、
 * {@code batch.trend.enabled=true} の場合にのみBean登録され、{@code batch.trend.cron} に従って
 * 全プラットフォーム対象の{@link TrendAnalysisApplicationService#analyze}を呼び出す。
 * デフォルトは無効（false）。
 */
@Component
@ConditionalOnProperty(prefix = "batch.trend", name = "enabled", havingValue = "true")
public class TrendAnalysisScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrendAnalysisScheduler.class);

    private final TrendAnalysisApplicationService trendAnalysisApplicationService;
    private final BatchTrendProperties properties;

    public TrendAnalysisScheduler(TrendAnalysisApplicationService trendAnalysisApplicationService,
                                   BatchTrendProperties properties) {
        this.trendAnalysisApplicationService = trendAnalysisApplicationService;
        this.properties = properties;
        log.info("Trend analysis daily batch enabled. cron='{}'", properties.getCron());
    }

    @Scheduled(cron = "${batch.trend.cron:0 0 3 * * *}")
    public void run() {
        log.info("Trend analysis daily batch started");
        TrendReportDto report = trendAnalysisApplicationService.analyze(
                new TrendAnalysisRequest(null, properties.getRecentWindowDays(), properties.getBaselineWindowDays()));
        log.info("Trend analysis daily batch completed: {} items detected", report.items().size());
    }
}
