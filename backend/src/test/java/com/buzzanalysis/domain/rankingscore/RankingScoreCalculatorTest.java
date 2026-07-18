package com.buzzanalysis.domain.rankingscore;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.rankingscore.strategy.RankingScoreStrategy;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RankingScoreCalculatorTest {

    private final Post post = new Post(UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
            "https://instagram.com/p/1", OffsetDateTime.now(), "creator", "caption", List.of(),
            10L, 2L, 100L, null, null, null, PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());

    @Test
    void calculate_computesWeightedAverage() {
        RankingScoreStrategy high = fixedStrategy("high", 0.5, 100.0);
        RankingScoreStrategy low = fixedStrategy("low", 0.5, 20.0);
        RankingScoreCalculator calculator = new RankingScoreCalculator(List.of(high, low));

        RankingScoreCalculator.CalculationResult result = calculator.calculate(
                new RankingScoreInput(post, null, null, null, null));

        assertThat(result.rankingScore()).isEqualTo(60.0);
    }

    @Test
    void constructor_rejectsEmptyStrategyList() {
        assertThatThrownBy(() -> new RankingScoreCalculator(List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    private RankingScoreStrategy fixedStrategy(String name, double weight, double score) {
        return new RankingScoreStrategy() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public double weight() {
                return weight;
            }

            @Override
            public double score(RankingScoreInput input) {
                return score;
            }
        };
    }
}
