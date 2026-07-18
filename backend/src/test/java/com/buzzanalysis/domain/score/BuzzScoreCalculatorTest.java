package com.buzzanalysis.domain.score;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.score.strategy.BuzzScoreStrategy;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuzzScoreCalculatorTest {

    @Test
    void calculate_computesWeightedAverageAcrossStrategies() {
        BuzzScoreStrategy strategyA = fixedStrategy("a", 0.5, 80.0);
        BuzzScoreStrategy strategyB = fixedStrategy("b", 0.5, 40.0);
        BuzzScoreCalculator calculator = new BuzzScoreCalculator(List.of(strategyA, strategyB));

        BuzzScoreCalculator.CalculationResult result = calculator.calculate(BuzzScoreInput.withoutAi(samplePost()));

        // (80*0.5 + 40*0.5) / (0.5+0.5) = 60.0
        assertThat(result.totalScore()).isEqualTo(60.0);
        assertThat(result.breakdown()).containsEntry("a", 80.0).containsEntry("b", 40.0);
    }

    @Test
    void calculate_clampsOutOfRangeStrategyScoresTo0to100() {
        BuzzScoreStrategy tooHigh = fixedStrategy("tooHigh", 1.0, 150.0);
        BuzzScoreCalculator calculator = new BuzzScoreCalculator(List.of(tooHigh));

        BuzzScoreCalculator.CalculationResult result = calculator.calculate(BuzzScoreInput.withoutAi(samplePost()));

        assertThat(result.totalScore()).isEqualTo(100.0);
    }

    @Test
    void constructor_rejectsEmptyStrategyList() {
        assertThrows(IllegalArgumentException.class, () -> new BuzzScoreCalculator(List.of()));
    }

    @Test
    void calculate_withAllProductionStrategies_returnsScoreWithinBounds() {
        BuzzScoreCalculator calculator = new BuzzScoreCalculator(List.of(
                fixedStrategy("engagementRate", 0.20, 72.5),
                fixedStrategy("viewCount", 0.15, 68.2),
                fixedStrategy("commentRate", 0.10, 45.1),
                fixedStrategy("postFormat", 0.10, 100.0),
                fixedStrategy("hashtag", 0.10, 100.0),
                fixedStrategy("postTiming", 0.10, 100.0),
                fixedStrategy("contentStructure", 0.10, 85.0),
                fixedStrategy("aiAnalysis", 0.15, 75.0)
        ));

        BuzzScoreCalculator.CalculationResult result = calculator.calculate(BuzzScoreInput.withoutAi(samplePost()));

        assertThat(result.totalScore()).isBetween(0.0, 100.0);
        assertThat(result.breakdown()).hasSize(8);
    }

    private BuzzScoreStrategy fixedStrategy(String name, double weight, double score) {
        return new BuzzScoreStrategy() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public double weight() {
                return weight;
            }

            @Override
            public double score(BuzzScoreInput input) {
                return score;
            }
        };
    }

    private Post samplePost() {
        return new Post(
                UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
                "https://www.instagram.com/reel/ext-1/", OffsetDateTime.now().minusDays(1), "creator",
                "sample caption", List.of("tag1", "tag2"), 1000L, 100L, 10000L, 50L,
                30, null, PostType.REEL, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }
}
