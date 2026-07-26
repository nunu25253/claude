package com.buzzanalysis.application.embedding;

import com.buzzanalysis.application.embedding.dto.EmbeddingDto;
import com.buzzanalysis.application.embedding.dto.EmbeddingGenerationSummaryDto;
import com.buzzanalysis.application.preprocessing.PreprocessingApplicationService;
import com.buzzanalysis.application.preprocessing.dto.PreprocessedPostDto;
import com.buzzanalysis.domain.embedding.Embedding;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingRepository;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 「Embedding生成」ユースケース（Phase3）。Phase2の前処理結果（本文・ハッシュタグ）を
 * OpenAI Embeddings APIでベクトル化し、pgvectorへ保存する。
 *
 * <p><b>対応対象:</b> {@link EmbeddingTarget#BODY} と {@link EmbeddingTarget#HASHTAGS} のみ。
 * {@code TITLE}（独立したタイトルフィールドが存在しない）と {@code COMMENT_SUMMARY}
 * （コメント本文自体を収集していない）は、{@code docs/phases/phase3_embeddings.md} に記載の
 * 理由により現時点では非対応（{@link #resolveSourceText} が明示的に例外を投げる）。</p>
 *
 * <p><b>コスト対策:</b> 前回生成時と {@code sourceText} が変わっていない対象はAPI呼び出しをスキップする。</p>
 */
@Service
public class EmbeddingGenerationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingGenerationApplicationService.class);

    /** 現時点でEmbedding生成に対応しているターゲット。 */
    static final List<EmbeddingTarget> SUPPORTED_TARGETS = List.of(EmbeddingTarget.BODY, EmbeddingTarget.HASHTAGS);

    private final PreprocessingApplicationService preprocessingApplicationService;
    private final EmbeddingClient embeddingClient;
    private final EmbeddingRepository embeddingRepository;

    public EmbeddingGenerationApplicationService(PreprocessingApplicationService preprocessingApplicationService,
                                                  EmbeddingClient embeddingClient,
                                                  EmbeddingRepository embeddingRepository) {
        this.preprocessingApplicationService = preprocessingApplicationService;
        this.embeddingClient = embeddingClient;
        this.embeddingRepository = embeddingRepository;
    }

    @Transactional
    public EmbeddingGenerationSummaryDto generateForPost(UUID postId) {
        PreprocessedPostDto preprocessed = preprocessingApplicationService.preprocess(postId);

        List<EmbeddingTarget> generated = new ArrayList<>();
        List<EmbeddingTarget> skipped = new ArrayList<>();
        List<EmbeddingDto> results = new ArrayList<>();

        for (EmbeddingTarget target : SUPPORTED_TARGETS) {
            String sourceText = resolveSourceText(preprocessed, target);
            if (sourceText == null || sourceText.isBlank()) {
                log.debug("Skipping embedding target={} for post={}: source text is empty", target, postId);
                continue;
            }

            Optional<Embedding> existing = embeddingRepository.findByPostIdAndTarget(postId, target);
            if (existing.isPresent() && sourceText.equals(existing.get().getSourceText())) {
                skipped.add(target);
                results.add(EmbeddingDto.from(existing.get()));
                continue;
            }

            EmbeddingResult result = embeddingClient.embed(sourceText);
            Embedding embedding = Embedding.createNew(postId, target, result, sourceText);
            Embedding saved = embeddingRepository.save(embedding);
            generated.add(target);
            results.add(EmbeddingDto.from(saved));
        }

        return new EmbeddingGenerationSummaryDto(postId, generated, skipped, results);
    }

    @Transactional(readOnly = true)
    public List<EmbeddingDto> getEmbeddings(UUID postId) {
        return embeddingRepository.findAllByPostId(postId).stream().map(EmbeddingDto::from).toList();
    }

    /**
     * 指定ターゲットの埋め込み元テキストを解決する。{@code TITLE}/{@code COMMENT_SUMMARY} は現時点で
     * 非対応のため例外を投げる（{@code docs/phases/phase3_embeddings.md} 参照）。パッケージ内テストから
     * 直接検証できるよう可視性を package-private にしている。
     */
    String resolveSourceText(PreprocessedPostDto preprocessed, EmbeddingTarget target) {
        return switch (target) {
            case BODY -> preprocessed.cleanText();
            case HASHTAGS -> String.join(" ", preprocessed.hashtags());
            case TITLE -> throw new UnsupportedOperationException(
                    "EmbeddingTarget.TITLE is not yet supported: Instagram/TikTok/X posts have no distinct "
                            + "title field. See docs/phases/phase3_embeddings.md for the rationale and alternative.");
            case COMMENT_SUMMARY -> throw new UnsupportedOperationException(
                    "EmbeddingTarget.COMMENT_SUMMARY is not yet supported: comment text itself is not collected "
                            + "(only comment counts). See docs/phases/phase3_embeddings.md for the rationale and alternative.");
        };
    }
}
