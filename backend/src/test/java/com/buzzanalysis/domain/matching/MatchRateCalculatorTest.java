package com.buzzanalysis.domain.matching;

import com.buzzanalysis.domain.matching.strategy.MatchRateStrategy;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchRateCalculatorTest {

    private final Post post = new Post(UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
            "https://instagram.com/p/1", OffsetDateTime.now(), "creator", "caption", List.of(),
            10L, 2L, 100L, null, null, null, PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());

    @Test
    void calculate_computesWeightedAverageAcrossStrategies() {
        MatchRateStrategy full = fixedStrategy("full", 0.5, 100.0);
        MatchRateStrategy zero = fixedStrategy("zero", 0.5, 0.0);
        MatchRateCalculator calculator = new MatchRateCalculator(List.of(full, zero));

        MatchRateCalculator.CalculationResult result = calculator.calculate(
                new MatchRateInput(post, null, null, UserSearchCondition.builder().build(), null));

        assertThat(result.matchRatePercent()).isEqualTo(50.0);
        assertThat(result.breakdown()).containsEntry("full", 100.0).containsEntry("zero", 0.0);
    }

    @Test
    void calculate_clampsOutOfRangeScores() {
        MatchRateStrategy tooHigh = fixedStrategy("tooHigh", 1.0, 150.0);
        MatchRateCalculator calculator = new MatchRateCalculator(List.of(tooHigh));

        MatchRateCalculator.CalculationResult result = calculator.calculate(
                new MatchRateInput(post, null, null, UserSearchCondition.builder().build(), null));

        assertThat(result.matchRatePercent()).isEqualTo(100.0);
    }

    @Test
    void constructor_rejectsEmptyStrategyList() {
        assertThatThrownBy(() -> new MatchRateCalculator(List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    private MatchRateStrategy fixedStrategy(String name, double weight, double score) {
        return new MatchRateStrategy() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public double weight() {
                return weight;
            }

            @Override
            public double score(MatchRateInput input) {
                return score;
            }
        };
    }
}
