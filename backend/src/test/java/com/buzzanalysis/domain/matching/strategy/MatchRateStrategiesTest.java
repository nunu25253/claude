package com.buzzanalysis.domain.matching.strategy;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.matching.MatchRateInput;
import com.buzzanalysis.domain.matching.UserSearchCondition;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.preprocessing.DurationCategory;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import com.buzzanalysis.domain.preprocessing.VideoDurationInfo;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Phase6の各{@link MatchRateStrategy}実装の単体テスト。 */
class MatchRateStrategiesTest {

    private final Post post = new Post(UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
            "https://instagram.com/p/1", OffsetDateTime.now(), "creator", "caption", List.of("tag"),
            10L, 2L, 100L, null, 30, null, PostType.REEL, OffsetDateTime.now(), OffsetDateTime.now());

    // ---- SemanticSimilarityMatchStrategy ----

    @Test
    void semanticSimilarity_usesProvidedSimilarity_whenKeywordSpecified() {
        SemanticSimilarityMatchStrategy strategy = new SemanticSimilarityMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().keyword("楽天カード").build();

        double score = strategy.score(new MatchRateInput(post, null, null, condition, 0.8));

        assertThat(score).isEqualTo(80.0);
    }

    @Test
    void semanticSimilarity_returnsNeutral_whenNoFreeTextCondition() {
        SemanticSimilarityMatchStrategy strategy = new SemanticSimilarityMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().build();

        double score = strategy.score(new MatchRateInput(post, null, null, condition, null));

        assertThat(score).isEqualTo(50.0);
    }

    // ---- GenreMatchStrategy ----

    @Test
    void genre_returnsFullScore_whenGenreAndSubGenreBothMatch() {
        GenreMatchStrategy strategy = new GenreMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().genre("美容").subGenre("スキンケア").build();
        AnalysisResult analysis = AnalysisResult.builder().postId(post.getId()).genre("美容").subGenre("スキンケア").build();

        double score = strategy.score(new MatchRateInput(post, analysis, null, condition, null));

        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void genre_returnsNotAnalyzedScore_whenAnalysisMissing() {
        GenreMatchStrategy strategy = new GenreMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().genre("美容").build();

        double score = strategy.score(new MatchRateInput(post, null, null, condition, null));

        assertThat(score).isEqualTo(30.0);
    }

    // ---- HookCtaPresenceMatchStrategy ----

    @Test
    void hookCtaPresence_returnsFullScore_whenBothPresent() {
        HookCtaPresenceMatchStrategy strategy = new HookCtaPresenceMatchStrategy();
        AnalysisResult analysis = AnalysisResult.builder().postId(post.getId())
                .hook("冒頭のフック文").callToAction("プロフィールへ誘導").build();

        double score = strategy.score(new MatchRateInput(post, analysis, null, UserSearchCondition.builder().build(), null));

        assertThat(score).isEqualTo(100.0);
    }

    // ---- TargetAudienceMatchStrategy ----

    @Test
    void targetAudience_returnsFullScore_whenAgeAndGenderBothFoundInText() {
        TargetAudienceMatchStrategy strategy = new TargetAudienceMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().targetAgeRange("20代").targetGender("女性").build();
        AnalysisResult analysis = AnalysisResult.builder().postId(post.getId())
                .targetAudience("20代女性、スイーツ好き").build();

        double score = strategy.score(new MatchRateInput(post, analysis, null, condition, null));

        assertThat(score).isEqualTo(100.0);
    }

    // ---- PostFormatMatchStrategy ----

    @Test
    void postFormat_returnsFullScore_whenContentFormatMatches() {
        PostFormatMatchStrategy strategy = new PostFormatMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().postFormat(ContentFormat.SHORT_VIDEO).build();
        PreprocessedPost preprocessed = PreprocessedPost.builder().postId(post.getId())
                .contentFormat(ContentFormat.SHORT_VIDEO).build();

        double score = strategy.score(new MatchRateInput(post, null, preprocessed, condition, null));

        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void postFormat_returnsLowScore_whenContentFormatDiffers() {
        PostFormatMatchStrategy strategy = new PostFormatMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().postFormat(ContentFormat.TEXT_ONLY).build();
        PreprocessedPost preprocessed = PreprocessedPost.builder().postId(post.getId())
                .contentFormat(ContentFormat.SHORT_VIDEO).build();

        double score = strategy.score(new MatchRateInput(post, null, preprocessed, condition, null));

        assertThat(score).isEqualTo(15.0);
    }

    // ---- VideoDurationMatchStrategy ----

    @Test
    void videoDuration_returnsFullScore_whenCloseToRequestedDuration() {
        VideoDurationMatchStrategy strategy = new VideoDurationMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().videoDurationSeconds(30).build();
        PreprocessedPost preprocessed = PreprocessedPost.builder().postId(post.getId())
                .videoDuration(new VideoDurationInfo(32, DurationCategory.MEDIUM)).build();

        double score = strategy.score(new MatchRateInput(post, null, preprocessed, condition, null));

        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void videoDuration_returnsLowScore_whenPostHasNoVideo() {
        VideoDurationMatchStrategy strategy = new VideoDurationMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().videoDurationSeconds(30).build();
        PreprocessedPost preprocessed = PreprocessedPost.builder().postId(post.getId())
                .videoDuration(VideoDurationInfo.notApplicable()).build();

        double score = strategy.score(new MatchRateInput(post, null, preprocessed, condition, null));

        assertThat(score).isEqualTo(10.0);
    }

    // ---- PurposeMatchStrategy ----

    @Test
    void purpose_returnsFullScore_whenPurposeTextContainsRequestedPurpose() {
        PurposeMatchStrategy strategy = new PurposeMatchStrategy();
        UserSearchCondition condition = UserSearchCondition.builder().purpose("認知獲得").build();
        AnalysisResult analysis = AnalysisResult.builder().postId(post.getId()).postPurpose("認知獲得").build();

        double score = strategy.score(new MatchRateInput(post, analysis, null, condition, null));

        assertThat(score).isEqualTo(100.0);
    }
}
