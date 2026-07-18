package com.buzzanalysis.domain.rankingscore.strategy;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.preprocessing.DurationCategory;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import com.buzzanalysis.domain.preprocessing.VideoDurationInfo;
import com.buzzanalysis.domain.rankingscore.RankingScoreInput;
import com.buzzanalysis.domain.score.BuzzScore;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Phase7の各{@link RankingScoreStrategy}実装の単体テスト。 */
class RankingScoreStrategiesTest {

    private final Post post = new Post(UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
            "https://instagram.com/p/1", OffsetDateTime.now(), "creator", "caption", List.of(),
            100L, 10L, 1000L, null, 30, null, PostType.REEL, OffsetDateTime.now(), OffsetDateTime.now());

    @Test
    void matchRateRanking_usesProvidedMatchRate() {
        MatchRateRankingStrategy strategy = new MatchRateRankingStrategy();
        double score = strategy.score(new RankingScoreInput(post, null, null, null, 82.0));
        assertThat(score).isEqualTo(82.0);
    }

    @Test
    void matchRateRanking_returnsNeutral_whenNoMatchRate() {
        MatchRateRankingStrategy strategy = new MatchRateRankingStrategy();
        double score = strategy.score(new RankingScoreInput(post, null, null, null, null));
        assertThat(score).isEqualTo(50.0);
    }

    @Test
    void buzzScoreRanking_usesExistingBuzzScore() {
        BuzzScoreRankingStrategy strategy = new BuzzScoreRankingStrategy();
        BuzzScore buzzScore = BuzzScore.of(post.getId(), 73.5, Map.of());
        double score = strategy.score(new RankingScoreInput(post, null, null, buzzScore, null));
        assertThat(score).isEqualTo(73.5);
    }

    @Test
    void likeRateRanking_scoresHigherForHigherLikeRatio() {
        LikeRateRankingStrategy strategy = new LikeRateRankingStrategy();
        double score = strategy.score(new RankingScoreInput(post, null, null, null, null));
        // likes=100, views=1000 -> rate=0.10 -> saturation(0.10) -> 100点
        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void likeRateRanking_returnsConservativeScore_whenViewsUnmeasured() {
        Post noViews = new Post(UUID.randomUUID(), UUID.randomUUID(), Platform.X, "ext-2",
                "https://x.com/status/2", OffsetDateTime.now(), "creator", "caption", List.of(),
                50L, 5L, null, null, null, null, PostType.TEXT, OffsetDateTime.now(), OffsetDateTime.now());
        LikeRateRankingStrategy strategy = new LikeRateRankingStrategy();
        double score = strategy.score(new RankingScoreInput(noViews, null, null, null, null));
        assertThat(score).isEqualTo(30.0);
    }

    @Test
    void freshnessRanking_scoresHigherForRecentPosts() {
        FreshnessRankingStrategy strategy = new FreshnessRankingStrategy();
        Post recent = postPublishedAt(OffsetDateTime.now().minusHours(1));
        Post old = postPublishedAt(OffsetDateTime.now().minusDays(60));

        double recentScore = strategy.score(new RankingScoreInput(recent, null, null, null, null));
        double oldScore = strategy.score(new RankingScoreInput(old, null, null, null, null));

        assertThat(recentScore).isCloseTo(100.0, within(2.0));
        assertThat(recentScore).isGreaterThan(oldScore);
    }

    @Test
    void videoDurationRanking_returnsFullScore_whenWithinOptimalRange() {
        VideoDurationRankingStrategy strategy = new VideoDurationRankingStrategy();
        PreprocessedPost preprocessed = PreprocessedPost.builder().postId(post.getId())
                .videoDuration(new VideoDurationInfo(30, DurationCategory.MEDIUM)).build();

        double score = strategy.score(new RankingScoreInput(post, null, preprocessed, null, null));

        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void videoDurationRanking_returnsNeutral_whenNoVideo() {
        VideoDurationRankingStrategy strategy = new VideoDurationRankingStrategy();
        PreprocessedPost preprocessed = PreprocessedPost.builder().postId(post.getId())
                .videoDuration(VideoDurationInfo.notApplicable()).build();

        double score = strategy.score(new RankingScoreInput(post, null, preprocessed, null, null));

        assertThat(score).isEqualTo(50.0);
    }

    private Post postPublishedAt(OffsetDateTime publishedAt) {
        return new Post(UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-x",
                "https://instagram.com/p/x", publishedAt, "creator", "caption", List.of(),
                10L, 1L, 100L, null, null, null, PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
