package com.buzzanalysis.domain.commonality;

import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.preprocessing.DurationCategory;
import com.buzzanalysis.domain.preprocessing.Language;
import com.buzzanalysis.domain.preprocessing.PostingTimeInfo;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import com.buzzanalysis.domain.preprocessing.TimeSlot;
import com.buzzanalysis.domain.preprocessing.VideoDurationInfo;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommonalityStatisticsCalculatorTest {

    private final CommonalityStatisticsCalculator calculator = new CommonalityStatisticsCalculator();

    @Test
    void topHashtags_returnsMostFrequentHashtagsInOrder() {
        List<PreprocessedPost> posts = List.of(
                post(List.of("a", "b"), null, null, null),
                post(List.of("a", "c"), null, null, null),
                post(List.of("a", "b"), null, null, null)
        );

        List<String> top = calculator.topHashtags(posts);

        assertThat(top.get(0)).isEqualTo("a");
        assertThat(top).contains("b", "c");
    }

    @Test
    void medianVideoDurationSeconds_computesMedianAcrossVideoPosts() {
        List<PreprocessedPost> posts = List.of(
                post(List.of(), 10, null, null),
                post(List.of(), 20, null, null),
                post(List.of(), 30, null, null)
        );

        assertThat(calculator.medianVideoDurationSeconds(posts)).isEqualTo(20);
    }

    @Test
    void medianVideoDurationSeconds_returnsNull_whenNoVideoPosts() {
        List<PreprocessedPost> posts = List.of(post(List.of(), null, null, null));
        assertThat(calculator.medianVideoDurationSeconds(posts)).isNull();
    }

    @Test
    void mostCommonPostingHour_returnsHourWithHighestFrequency() {
        List<PreprocessedPost> posts = List.of(
                post(List.of(), null, 19, null),
                post(List.of(), null, 19, null),
                post(List.of(), null, 9, null)
        );

        assertThat(calculator.mostCommonPostingHour(posts)).isEqualTo(19);
    }

    @Test
    void mostCommonContentFormat_returnsMostFrequentFormat() {
        List<PreprocessedPost> posts = List.of(
                post(List.of(), null, null, ContentFormat.SHORT_VIDEO),
                post(List.of(), null, null, ContentFormat.SHORT_VIDEO),
                post(List.of(), null, null, ContentFormat.TEXT_ONLY)
        );

        assertThat(calculator.mostCommonContentFormat(posts)).isEqualTo(ContentFormat.SHORT_VIDEO);
    }

    private PreprocessedPost post(List<String> hashtags, Integer videoDurationSeconds, Integer postingHour,
                                   ContentFormat contentFormat) {
        return PreprocessedPost.builder()
                .postId(UUID.randomUUID())
                .cleanText("text")
                .language(Language.JAPANESE)
                .hashtags(hashtags)
                .postingTime(postingHour == null ? null
                        : new PostingTimeInfo(postingHour, DayOfWeek.MONDAY, false, TimeSlot.EVENING))
                .videoDuration(videoDurationSeconds == null ? VideoDurationInfo.notApplicable()
                        : new VideoDurationInfo(videoDurationSeconds, DurationCategory.MEDIUM))
                .contentFormat(contentFormat)
                .build();
    }
}
