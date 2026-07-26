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

class HashtagStrategyTest {

    private final HashtagStrategy strategy = new HashtagStrategy();

    @Test
    void score_isMax_whenHashtagCountWithinOptimalRange() {
        assertThat(strategy.score(BuzzScoreInput.withoutAi(postWithHashtags(5)))).isEqualTo(100.0);
    }

    @Test
    void score_isLow_whenNoHashtags() {
        assertThat(strategy.score(BuzzScoreInput.withoutAi(postWithHashtags(0)))).isEqualTo(20.0);
    }

    @Test
    void score_decreases_whenTooManyHashtags() {
        double score = strategy.score(BuzzScoreInput.withoutAi(postWithHashtags(30)));
        assertThat(score).isLessThan(100.0);
    }

    private Post postWithHashtags(int count) {
        List<String> hashtags = java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> "tag" + i)
                .toList();
        return new Post(
                UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
                "https://www.instagram.com/reel/ext-1/", OffsetDateTime.now(), "creator",
                "caption", hashtags, 100L, 10L, 1000L, null,
                null, null, PostType.REEL, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }
}
