package com.buzzanalysis.application.semanticsearch;

import com.buzzanalysis.application.post.dto.PostDto;
import com.buzzanalysis.application.semanticsearch.dto.SemanticSearchResultDto;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingRepository;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
import com.buzzanalysis.domain.embedding.SimilarityMatch;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 「意味検索」ユースケース（Phase4）。キーワードをOpenAI Embeddings APIでベクトル化し、pgvectorの
 * コサイン距離演算子でBODY Embeddingとの類似度が近い投稿を検索する。文字列としては一致しない
 * 意味的に近い投稿（例:「楽天カード」検索で「ポイ活」「SPU」等の投稿）も検索結果に含まれる。
 *
 * <p><b>制約:</b> Phase3でBODY Embeddingが生成済みの投稿のみが検索対象となる
 * （{@code docs/phases/phase4_semantic_search.md} 参照）。</p>
 */
@Service
public class SemanticSearchApplicationService {

    private final EmbeddingClient embeddingClient;
    private final EmbeddingRepository embeddingRepository;
    private final PostRepository postRepository;

    public SemanticSearchApplicationService(EmbeddingClient embeddingClient, EmbeddingRepository embeddingRepository,
                                             PostRepository postRepository) {
        this.embeddingClient = embeddingClient;
        this.embeddingRepository = embeddingRepository;
        this.postRepository = postRepository;
    }

    @Cacheable(value = "semanticSearchResults", key = "#keyword + ':' + #limit")
    @Transactional(readOnly = true)
    public List<SemanticSearchResultDto> search(String keyword, int limit) {
        EmbeddingResult queryEmbedding = embeddingClient.embed(keyword);
        List<SimilarityMatch> matches = embeddingRepository.findNearest(EmbeddingTarget.BODY,
                queryEmbedding.vector(), limit);

        List<SemanticSearchResultDto> results = new ArrayList<>();
        for (SimilarityMatch match : matches) {
            Optional<Post> post = postRepository.findById(match.postId());
            post.ifPresent(p -> results.add(new SemanticSearchResultDto(
                    PostDto.from(p), match.similarity(), toMatchRatePercent(match.similarity()))));
        }
        return results;
    }

    /** コサイン類似度(0.0〜1.0を想定)を0〜100の一致率にクランプして変換する。 */
    private double toMatchRatePercent(double similarity) {
        double percent = similarity * 100.0;
        double clamped = Math.max(0.0, Math.min(100.0, percent));
        return Math.round(clamped * 100.0) / 100.0;
    }
}
