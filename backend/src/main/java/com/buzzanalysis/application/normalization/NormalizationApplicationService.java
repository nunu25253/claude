package com.buzzanalysis.application.normalization;

import com.buzzanalysis.application.normalization.dto.NormalizedPostDto;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 「投稿データの正規化」ユースケース（Phase1）。既存の {@code Post} 集約を取得し、
 * {@link PostNormalizer} でプラットフォーム非依存の {@link NormalizedPostDto} に変換する。
 * Phase2以降のAI分析パイプラインは、このサービスが返すDTOを唯一の入力契約として利用する想定。
 */
@Service
public class NormalizationApplicationService {

    private final PostRepository postRepository;
    private final PostNormalizer postNormalizer;

    public NormalizationApplicationService(PostRepository postRepository, PostNormalizer postNormalizer) {
        this.postRepository = postRepository;
        this.postNormalizer = postNormalizer;
    }

    @Transactional(readOnly = true)
    public NormalizedPostDto normalize(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> EntityNotFoundException.of("Post", postId));
        return NormalizedPostDto.from(postNormalizer.normalize(post));
    }
}
