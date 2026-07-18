package com.buzzanalysis.application.event;

import com.buzzanalysis.domain.analysis.AnalysisCompletedEvent;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observerパターンの実装: {@link AnalysisCompletedEvent} を購読し、
 * (1) 当該アカウントの競合統計/ランキングキャッシュを無効化する、
 * (2) 分析完了をログ出力し将来的な自動レポート生成トリガーの起点とする。
 * Spring の {@code ApplicationEventPublisher}/{@code @EventListener} を使ったPub-Sub実装であり、
 * 発行元（application/post 配下のユースケース）とは疎結合。
 */
@Component
public class AnalysisCompletedEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnalysisCompletedEventListener.class);

    private final PostRepository postRepository;
    private final CacheManager cacheManager;

    public AnalysisCompletedEventListener(PostRepository postRepository, CacheManager cacheManager) {
        this.postRepository = postRepository;
        this.cacheManager = cacheManager;
    }

    @Async
    @EventListener
    public void onAnalysisCompleted(AnalysisCompletedEvent event) {
        log.info("Analysis completed for post={} analysisResult={} buzzScore={}",
                event.getPostId(), event.getAnalysisResultId(), event.getBuzzScore());

        evictCompetitorStatsCache(event);
        evictRankingsCache();
        // 将来的な拡張ポイント: ここでレポート自動生成（GenerateReportCommand経由）や
        // ユーザーへの通知（メール/Push）をトリガーすることも可能。
    }

    private void evictCompetitorStatsCache(AnalysisCompletedEvent event) {
        postRepository.findById(event.getPostId()).map(Post::getSocialAccountId).ifPresent(accountId -> {
            Cache cache = cacheManager.getCache("competitorStats");
            if (cache != null) {
                cache.evict(accountId);
                log.debug("Evicted competitorStats cache for account={}", accountId);
            }
        });
    }

    private void evictRankingsCache() {
        Cache cache = cacheManager.getCache("rankings");
        if (cache != null) {
            cache.clear();
            log.debug("Cleared rankings cache");
        }
    }
}
