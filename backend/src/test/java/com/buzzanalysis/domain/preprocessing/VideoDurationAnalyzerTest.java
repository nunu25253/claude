package com.buzzanalysis.domain.preprocessing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VideoDurationAnalyzerTest {

    private final VideoDurationAnalyzer analyzer = new VideoDurationAnalyzer();

    @Test
    void analyze_classifiesShortMediumLongExtended() {
        assertThat(analyzer.analyze(10).category()).isEqualTo(DurationCategory.SHORT);
        assertThat(analyzer.analyze(45).category()).isEqualTo(DurationCategory.MEDIUM);
        assertThat(analyzer.analyze(150).category()).isEqualTo(DurationCategory.LONG);
        assertThat(analyzer.analyze(300).category()).isEqualTo(DurationCategory.EXTENDED);
    }

    @Test
    void analyze_returnsNotApplicable_whenNoVideo() {
        VideoDurationInfo result = analyzer.analyze(null);

        assertThat(result.durationSeconds()).isNull();
        assertThat(result.category()).isNull();
    }
}
