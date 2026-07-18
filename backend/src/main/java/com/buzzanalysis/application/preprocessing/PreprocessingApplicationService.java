package com.buzzanalysis.application.preprocessing;

import com.buzzanalysis.application.preprocessing.dto.PreprocessedPostDto;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 「AI分析用前処理」ユースケース（Phase2）。投稿を取得し、{@link PostNormalizer}（Phase1）で正規化した上で
 * {@link PostPreprocessor}（Phase2）によりテキストクレンジング・言語判定等を行い、DTOとして返す。
 */
@Service
public class PreprocessingApplicationService {

    private final PostRepository postRepository;
    private final PostNormalizer postNormalizer;
    private final PostPreprocessor postPreprocessor;

    public PreprocessingApplicationService(PostRepository postRepository, PostNormalizer postNormalizer,
                                            PostPreprocessor postPreprocessor) {
        this.postRepository = postRepository;
        this.postNormalizer = postNormalizer;
        this.postPreprocessor = postPreprocessor;
    }

    @Transactional(readOnly = true)
    public PreprocessedPostDto preprocess(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> EntityNotFoundException.of("Post", postId));
        NormalizedPost normalizedPost = postNormalizer.normalize(post);
        return PreprocessedPostDto.from(postPreprocessor.preprocess(normalizedPost));
    }
}
