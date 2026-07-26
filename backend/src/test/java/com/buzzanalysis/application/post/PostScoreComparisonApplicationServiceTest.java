package com.buzzanalysis.application.post;

import com.buzzanalysis.application.post.dto.PostScoreComparisonDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.genre.GenreNormalizer;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostScoreComparisonApplicationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private BuzzScoreRepository buzzScoreRepository;

    private PostScoreComparisonApplicationService service;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        service = new PostScoreComparisonApplicationService(postRepository, analysisResultRepository,
                buzzScoreRepository, new GenreNormalizer());
        accountId = UUID.randomUUID();
    }

    @Test
    void compare_throwsEntityNotFound_whenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.compare(postId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void compare_returnsPreviousPostScore_fromTheSameAccountsMostRecentEarlierPost() {
        OffsetDateTime now = OffsetDateTime.now();
        Post current = post(UUID.randomUUID(), now);
        Post previous = post(UUID.randomUUID(), now.minusDays(1));
        when(postRepository.findById(current.getId())).thenReturn(Optional.of(current));
        when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(previous), 0, 5, 1))
                .thenReturn(new PostSearchResult(List.of(), 0, 500, 0));
        when(buzzScoreRepository.findByPostId(previous.getId()))
                .thenReturn(Optional.of(new BuzzScore(UUID.randomUUID(), previous.getId(), 60.0, java.util.Map.of(), now)));
        when(analysisResultRepository.findByPostId(current.getId())).thenReturn(Optional.empty());

        PostScoreComparisonDto result = service.compare(current.getId());

        assertThat(result.previousPostScore()).isEqualTo(60.0);
        assertThat(result.genreAverageScore()).isNull();
        assertThat(result.genreSampleSize()).isZero();
    }

    @Test
    void compare_returnsGenreAverage_excludingTheCurrentPostItself() {
        OffsetDateTime now = OffsetDateTime.now();
        Post current = post(UUID.randomUUID(), now);
        Post sameGenrePost = post(UUID.randomUUID(), now.minusDays(2));
        Post otherGenrePost = post(UUID.randomUUID(), now.minusDays(3));

        when(postRepository.findById(current.getId())).thenReturn(Optional.of(current));
        // 1回目: 直前投稿検索(アカウント絞り込み)、2回目: 同ジャンル平均集計(プラットフォーム絞り込み)
        when(postRepository.search(any()))
                .thenReturn(new PostSearchResult(List.of(), 0, 5, 0))
                .thenReturn(new PostSearchResult(List.of(current, sameGenrePost, otherGenrePost), 0, 500, 3));
        when(analysisResultRepository.findByPostId(current.getId()))
                .thenReturn(Optional.of(AnalysisResult.builder().id(UUID.randomUUID()).postId(current.getId()).genre("美容").build()));
        when(analysisResultRepository.findByPostIdIn(any())).thenReturn(List.of(
                AnalysisResult.builder().id(UUID.randomUUID()).postId(current.getId()).genre("美容").build(),
                AnalysisResult.builder().id(UUID.randomUUID()).postId(sameGenrePost.getId()).genre("コスメ").build(),
                AnalysisResult.builder().id(UUID.randomUUID()).postId(otherGenrePost.getId()).genre("グルメ").build()));
        when(buzzScoreRepository.findByPostIdIn(any())).thenReturn(List.of(
                new BuzzScore(UUID.randomUUID(), current.getId(), 90.0, java.util.Map.of(), now),
                new BuzzScore(UUID.randomUUID(), sameGenrePost.getId(), 70.0, java.util.Map.of(), now),
                new BuzzScore(UUID.randomUUID(), otherGenrePost.getId(), 10.0, java.util.Map.of(), now)));

        PostScoreComparisonDto result = service.compare(current.getId());

        // 「コスメ」は「美容」の表記ゆれとしてGenreNormalizerが同一視するため一致、「グルメ」は不一致
        assertThat(result.genreAverageScore()).isEqualTo(70.0);
        assertThat(result.genreSampleSize()).isEqualTo(1);
    }

    private Post post(UUID id, OffsetDateTime publishedAt) {
        return new Post(id, accountId, Platform.INSTAGRAM, "ext-" + id, "https://example.com/" + id,
                publishedAt, "creator", "caption", List.of(), 10L, 2L, 100L, null, null, null,
                PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
