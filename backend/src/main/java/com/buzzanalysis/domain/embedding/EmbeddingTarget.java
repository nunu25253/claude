package com.buzzanalysis.domain.embedding;

/**
 * Embedding生成対象の種別。{@code TITLE} と {@code COMMENT_SUMMARY} は将来の拡張点として列挙型には
 * 予約されているが、現時点では生成できない（{@code docs/phases/phase3_embeddings.md} 参照）。
 * <ul>
 *   <li>{@code TITLE}: Instagram/TikTok/Xの投稿には独立したタイトルフィールドが存在しないため未対応。
 *       YouTube収集アダプタ追加時に対応予定。</li>
 *   <li>{@code COMMENT_SUMMARY}: 本システムはコメント数のみを収集しており、コメント本文自体を
 *       収集していないため未対応。</li>
 * </ul>
 */
public enum EmbeddingTarget {
    TITLE,
    BODY,
    HASHTAGS,
    COMMENT_SUMMARY
}
