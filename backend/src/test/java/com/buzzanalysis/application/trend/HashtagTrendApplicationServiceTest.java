package com.buzzanalysis.application.trend;

import com.buzzanalysis.application.trend.dto.TrendHashtagDto;
import com.buzzanalysis.application.trend.dto.TrendResponseDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchResult;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashtagTrendApplicationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private BuzzScoreRepository buzzScoreRepository;

    private HashtagTrendApplicationService service;

    @BeforeEach
    void setUp() {
        service = new HashtagTrendApplicationService(postRepository, analysisResultRepository, buzzScoreRepository);
        when(analysisResultRepository.findByPostId(any())).thenReturn(Optional.empty());
        when(buzzScoreRepository.findByPostId(any())).thenReturn(Optional.empty());
    }

    @Test
    void getTrends_computesPositiveGrowthRate_forHashtagAppearingInBothWindows() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Post> posts = List.of(
                post(now.minusDays(1), List.of("美容")), post(now.minusDays(2), List.of("美容")),
                post(now.minusDays(15), List.of("美容"))
        );
        when(postRepository.search(any())).thenReturn(new PostSearchResult(posts, 0, 500, posts.size()));

        TrendResponseDto result = service.getTrends(null, null);

        assertThat(result.hashtags()).hasSize(1);
        TrendHashtagDto tag = result.hashtags().get(0);
        assertThat(tag.tag()).isEqualTo("美容");
        assertThat(tag.postCount()).isEqualTo(2);
        assertThat(tag.growthRate()).isEqualTo(100.0);
    }

    @Test
    void getTrends_capsGrowthRateAt100_whenBaselineCountIsZero() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Post> posts = List.of(post(now.minusDays(1), List.of("新企画")));
        when(postRepository.search(any())).thenReturn(new PostSearchResult(posts, 0, 500, posts.size()));

        TrendResponseDto result = service.getTrends(null, null);

        assertThat(result.hashtags()).hasSize(1);
        assertThat(result.hashtags().get(0).growthRate()).isEqualTo(100.0);
    }

    @Test
    void getTrends_excludesPostsOutsideBaselineWindow() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Post> posts = List.of(post(now.minusDays(1), List.of("直近")), post(now.minusDays(60), List.of("古い")));
        when(postRepository.search(any())).thenReturn(new PostSearchResult(posts, 0, 500, posts.size()));

        TrendResponseDto result = service.getTrends(null, null);

        assertThat(result.hashtags()).extracting(TrendHashtagDto::tag).containsExactly("直近");
    }

    @Test
    void getTrends_filtersByGenre_usingAnalysisResultLookup() {
        OffsetDateTime now = OffsetDateTime.now();
        Post beautyPost = post(now.minusDays(1), List.of("共通タグ"));
        Post foodPost = post(now.minusDays(1), List.of("共通タグ"));
        when(postRepository.search(any()))
                .thenReturn(new PostSearchResult(List.of(beautyPost, foodPost), 0, 500, 2));
        when(analysisResultRepository.findByPostId(beautyPost.getId()))
                .thenReturn(Optional.of(AnalysisResult.builder().postId(beautyPost.getId()).genre("美容").build()));
        when(analysisResultRepository.findByPostId(foodPost.getId()))
                .thenReturn(Optional.of(AnalysisResult.builder().postId(foodPost.getId()).genre("グルメ").build()));

        TrendResponseDto result = service.getTrends(null, "美容");

        assertThat(result.hashtags()).hasSize(1);
        assertThat(result.hashtags().get(0).postCount()).isEqualTo(1);
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().get(0).id()).isEqualTo(beautyPost.getId());
    }

    @Test
    void getTrends_sortsPostsByBuzzScoreDescending() {
        OffsetDateTime now = OffsetDateTime.now();
        Post lowScorePost = post(now.minusDays(1), List.of("タグ"));
        Post highScorePost = post(now.minusDays(1), List.of("タグ"));
        when(postRepository.search(any()))
                .thenReturn(new PostSearchResult(List.of(lowScorePost, highScorePost), 0, 500, 2));
        when(buzzScoreRepository.findByPostId(lowScorePost.getId()))
                .thenReturn(Optional.of(BuzzScore.of(lowScorePost.getId(), 20.0, Map.of())));
        when(buzzScoreRepository.findByPostId(highScorePost.getId()))
                .thenReturn(Optional.of(BuzzScore.of(highScorePost.getId(), 90.0, Map.of())));

        TrendResponseDto result = service.getTrends(null, null);

        assertThat(result.posts()).extracting(p -> p.id())
                .containsExactly(highScorePost.getId(), lowScorePost.getId());
    }

    private Post post(OffsetDateTime publishedAt, List<String> hashtags) {
        return new Post(UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-" + UUID.randomUUID(),
                "https://instagram.com/p/x", publishedAt, "creator", "caption", hashtags,
                100L, 10L, null, null, null, 1, PostType.CAROUSEL, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
