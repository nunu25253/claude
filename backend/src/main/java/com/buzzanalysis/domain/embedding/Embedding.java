package com.buzzanalysis.domain.embedding;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 投稿の特定要素（本文/ハッシュタグ等）をOpenAI Embeddings APIでベクトル化した結果を表す集約。
 * {@code postId} + {@code target} の組で一意（同じ対象への再生成は既存レコードを更新する）。
 */
public class Embedding {

    private final UUID id;
    private final UUID postId;
    private final EmbeddingTarget target;
    private final float[] vector;
    private final String sourceText;
    private final String model;
    private final int dimensions;
    private final OffsetDateTime generatedAt;

    public Embedding(UUID id, UUID postId, EmbeddingTarget target, float[] vector, String sourceText,
                      String model, int dimensions, OffsetDateTime generatedAt) {
        this.id = id;
        this.postId = postId;
        this.target = target;
        this.vector = vector;
        this.sourceText = sourceText;
        this.model = model;
        this.dimensions = dimensions;
        this.generatedAt = generatedAt;
    }

    public static Embedding createNew(UUID postId, EmbeddingTarget target, EmbeddingResult result, String sourceText) {
        return new Embedding(UUID.randomUUID(), postId, target, result.vector(), sourceText,
                result.model(), result.dimensions(), OffsetDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public EmbeddingTarget getTarget() {
        return target;
    }

    public float[] getVector() {
        return vector;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getModel() {
        return model;
    }

    public int getDimensions() {
        return dimensions;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }
}
