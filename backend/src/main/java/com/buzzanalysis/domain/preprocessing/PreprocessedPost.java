package com.buzzanalysis.domain.preprocessing;

import java.util.List;
import java.util.UUID;

/**
 * AI分析用の前処理を終えた投稿データ（Phase2の成果物）。{@link com.buzzanalysis.domain.normalization.NormalizedPost}
 * を汚さず、独立した値オブジェクトとして出力する。すべてのフィールドは決定的なルール（正規表現・
 * ヒューリスティック判定・日時計算）によって導出され、AIによる生成値は一切含まない
 * （Phase5以降のAI分析結果と明確にレイヤーを分離するための設計原則）。
 *
 * @param postId          元となる投稿のID
 * @param cleanText       絵文字・URL・HTML除去、改行整理済みの本文
 * @param language        ヒューリスティック判定による言語（AIによる推定ではない）
 * @param hashtags        本文から再抽出したハッシュタグ一覧
 * @param mentions        本文から抽出したメンション一覧
 * @param postingTime     投稿時間解析の結果
 * @param videoDuration   動画時間解析の結果（動画を含まない場合は{@code durationSeconds}等がnull）
 * @param contentFormat   SNS横断で比較可能なコンテンツ形式
 */
public record PreprocessedPost(
        UUID postId,
        String cleanText,
        Language language,
        List<String> hashtags,
        List<String> mentions,
        PostingTimeInfo postingTime,
        VideoDurationInfo videoDuration,
        ContentFormat contentFormat
) {

    public static Builder builder() {
        return new Builder();
    }

    /** Builderパターンによる段階的な組み立て（`DefaultPostPreprocessor`が各解析結果を集約する）。 */
    public static final class Builder {
        private UUID postId;
        private String cleanText;
        private Language language;
        private List<String> hashtags = List.of();
        private List<String> mentions = List.of();
        private PostingTimeInfo postingTime;
        private VideoDurationInfo videoDuration;
        private ContentFormat contentFormat;

        public Builder postId(UUID postId) {
            this.postId = postId;
            return this;
        }

        public Builder cleanText(String cleanText) {
            this.cleanText = cleanText;
            return this;
        }

        public Builder language(Language language) {
            this.language = language;
            return this;
        }

        public Builder hashtags(List<String> hashtags) {
            this.hashtags = hashtags == null ? List.of() : hashtags;
            return this;
        }

        public Builder mentions(List<String> mentions) {
            this.mentions = mentions == null ? List.of() : mentions;
            return this;
        }

        public Builder postingTime(PostingTimeInfo postingTime) {
            this.postingTime = postingTime;
            return this;
        }

        public Builder videoDuration(VideoDurationInfo videoDuration) {
            this.videoDuration = videoDuration;
            return this;
        }

        public Builder contentFormat(ContentFormat contentFormat) {
            this.contentFormat = contentFormat;
            return this;
        }

        public PreprocessedPost build() {
            return new PreprocessedPost(postId, cleanText, language, hashtags, mentions, postingTime,
                    videoDuration, contentFormat);
        }
    }
}
