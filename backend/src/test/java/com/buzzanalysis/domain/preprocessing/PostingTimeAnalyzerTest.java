package com.buzzanalysis.domain.preprocessing;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class PostingTimeAnalyzerTest {

    private final PostingTimeAnalyzer analyzer = new PostingTimeAnalyzer();

    @Test
    void analyze_classifiesEveningWeekdayPost() {
        // 2026-07-17は金曜日、19時投稿
        OffsetDateTime publishedAt = OffsetDateTime.of(2026, 7, 17, 19, 30, 0, 0, ZoneOffset.UTC);

        PostingTimeInfo result = analyzer.analyze(publishedAt);

        assertThat(result.hour()).isEqualTo(19);
        assertThat(result.dayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(result.isWeekend()).isFalse();
        assertThat(result.timeSlot()).isEqualTo(TimeSlot.EVENING);
    }

    @Test
    void analyze_classifiesWeekendLateNightPost() {
        // 2026-07-18は土曜日、深夜2時投稿
        OffsetDateTime publishedAt = OffsetDateTime.of(2026, 7, 18, 2, 0, 0, 0, ZoneOffset.UTC);

        PostingTimeInfo result = analyzer.analyze(publishedAt);

        assertThat(result.isWeekend()).isTrue();
        assertThat(result.timeSlot()).isEqualTo(TimeSlot.LATE_NIGHT);
    }

    @Test
    void analyze_returnsNull_whenPublishedAtIsNull() {
        assertThat(analyzer.analyze(null)).isNull();
    }
}
