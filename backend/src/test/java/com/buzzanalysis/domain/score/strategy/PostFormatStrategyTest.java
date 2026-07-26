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

class PostFormatStrategyTest {

    private final PostFormatStrategy strategy = new PostFormatStrategy();

    @Test
    void reelFormat_scoresHigherThanTextFormat() {
        double reelScore = strategy.score(BuzzScoreInput.withoutAi(postOfType(PostType.REEL)));
        double textScore = strategy.score(BuzzScoreInput.withoutAi(postOfType(PostType.TEXT)));

        assertThat(reelScore).isGreaterThan(textScore);
    }

    @Test
    void videoFormat_scoresHigherThanImageFormat() {
        double videoScore = strategy.score(BuzzScoreInput.withoutAi(postOfType(PostType.VIDEO)));
        double imageScore = strategy.score(BuzzScoreInput.withoutAi(postOfType(PostType.IMAGE)));

        assertThat(videoScore).isGreaterThan(imageScore);
    }

    private Post postOfType(PostType type) {
        return new Post(
                UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
                "https://www.instagram.com/reel/ext-1/", OffsetDateTime.now(), "creator",
                "caption", List.of(), 100L, 10L, 1000L, null,
                null, null, type, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }
}
