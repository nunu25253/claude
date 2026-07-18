package com.buzzanalysis.application.preprocessing;

import com.buzzanalysis.application.preprocessing.dto.PreprocessedPostDto;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.preprocessing.Language;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreprocessingApplicationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostNormalizer postNormalizer;
    @Mock
    private PostPreprocessor postPreprocessor;

    private PreprocessingApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PreprocessingApplicationService(postRepository, postNormalizer, postPreprocessor);
    }

    @Test
    void preprocess_returnsDtoBuiltFromPreprocessor() {
        UUID postId = UUID.randomUUID();
        Post post = new Post(postId, UUID.randomUUID(), Platform.INSTAGRAM, "ext-1", "https://instagram.com/p/1",
                OffsetDateTime.now(), "creator", "caption", List.of("tag"), 10L, 2L, 100L, null, null, null,
                PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
        NormalizedPost normalized = NormalizedPost.builder().postId(postId).build();
        PreprocessedPost preprocessed = PreprocessedPost.builder()
                .postId(postId).cleanText("clean").language(Language.JAPANESE)
                .contentFormat(ContentFormat.SINGLE_IMAGE).build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postNormalizer.normalize(post)).thenReturn(normalized);
        when(postPreprocessor.preprocess(normalized)).thenReturn(preprocessed);

        PreprocessedPostDto result = service.preprocess(postId);

        assertThat(result).isEqualTo(PreprocessedPostDto.from(preprocessed));
        assertThat(result.cleanText()).isEqualTo("clean");
    }

    @Test
    void preprocess_throwsEntityNotFound_whenPostDoesNotExist() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.preprocess(postId)).isInstanceOf(EntityNotFoundException.class);
    }
}
