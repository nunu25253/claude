package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.score.BuzzScoreInput;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EngagementRateStrategyTest {

    private final EngagementRateStrategy strategy = new EngagementRateStrategy();

    @Test
    void score_returns100_whenEngagementRateAtOrAboveSaturation() {
        Post post = postWith(15_000L, 0L, 100_000L); // rate = 0.15 = saturation
        assertThat(strategy.score(BuzzScoreInput.withoutAi(post))).isEqualTo(100.0);
    }

    @Test
    void score_returns0_whenNoEngagementData() {
        Post post = postWith(null, null, null);
        assertThat(strategy.score(BuzzScoreInput.withoutAi(post))).isEqualTo(0.0);
    }

    @Test
    void score_usesCommentToLikeRatio_whenViewCountMissing() {
        Post post = postWith(1000L, 100L, null); // comments/likes = 0.1
        double score = strategy.score(BuzzScoreInput.withoutAi(post));
        assertThat(score).isGreaterThan(0.0).isLessThanOrEqualTo(100.0);
    }

    private Post postWith(Long likes, Long comments, Long views) {
        return new Post(
                UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
                "https://www.instagram.com/reel/ext-1/", OffsetDateTime.now(), "creator",
                "caption", List.of(), likes, comments, views, null,
                null, null, PostType.REEL, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }
}
