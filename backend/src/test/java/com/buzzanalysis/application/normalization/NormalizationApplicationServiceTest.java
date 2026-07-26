package com.buzzanalysis.application.normalization;

import com.buzzanalysis.application.normalization.dto.NormalizedPostDto;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link NormalizationApplicationService} のMockitoによる単体テスト。
 */
@ExtendWith(MockitoExtension.class)
class NormalizationApplicationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostNormalizer postNormalizer;

    private NormalizationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new NormalizationApplicationService(postRepository, postNormalizer);
    }

    @Test
    void normalize_returnsDtoBuiltFromNormalizer() {
        UUID postId = UUID.randomUUID();
        Post post = new Post(postId, UUID.randomUUID(), Platform.TIKTOK, "ext-1", "https://tiktok.com/x",
                OffsetDateTime.now(), "creator", "caption", List.of("tag"), 10L, 2L, 100L, 5L, 30, null,
                PostType.VIDEO, OffsetDateTime.now(), OffsetDateTime.now());
        NormalizedPost normalized = NormalizedPost.builder()
                .postId(postId).accountId(post.getSocialAccountId()).platform(Platform.TIKTOK)
                .postType(PostType.VIDEO).url(post.getUrl()).publishedAt(post.getPublishedAt())
                .authorName("creator").rawText("caption").hashtags(List.of("tag"))
                .likeCount(10L).commentCount(2L).viewCount(100L).shareCount(5L)
                .videoDurationSeconds(30).mediaCount(1).hasVideo(true).engagementRate(0.12)
                .postAgeInHours(0).unmeasuredMetrics(Set.of()).build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postNormalizer.normalize(post)).thenReturn(normalized);

        NormalizedPostDto result = service.normalize(postId);

        assertThat(result).isEqualTo(NormalizedPostDto.from(normalized));
        assertThat(result.viewCount()).isEqualTo(100L);
    }

    @Test
    void normalize_throwsEntityNotFound_whenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.normalize(postId)).isInstanceOf(EntityNotFoundException.class);
    }
}
