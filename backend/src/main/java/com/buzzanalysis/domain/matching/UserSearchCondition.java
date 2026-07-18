package com.buzzanalysis.domain.matching;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.preprocessing.ContentFormat;

/**
 * ユーザーが自由に設定する検索条件（Phase6: ユーザー条件分析）。すべての項目は任意（null許容）で、
 * 未指定の項目は各{@link com.buzzanalysis.domain.matching.strategy.MatchRateStrategy}で
 * 中立スコア（50点）として扱われる。
 */
public final class UserSearchCondition {

    private final String keyword;
    private final String productName;
    private final String brand;
    private final String aspOfferName;
    private final String genre;
    private final String subGenre;
    private final String targetAgeRange;
    private final String targetGender;
    private final ContentFormat postFormat;
    private final Platform platform;
    private final Integer videoDurationSeconds;
    private final String purpose;

    private UserSearchCondition(Builder b) {
        this.keyword = b.keyword;
        this.productName = b.productName;
        this.brand = b.brand;
        this.aspOfferName = b.aspOfferName;
        this.genre = b.genre;
        this.subGenre = b.subGenre;
        this.targetAgeRange = b.targetAgeRange;
        this.targetGender = b.targetGender;
        this.postFormat = b.postFormat;
        this.platform = b.platform;
        this.videoDurationSeconds = b.videoDurationSeconds;
        this.purpose = b.purpose;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** キーワード・商品名・ブランド・ASP案件名を意味検索用の1つのクエリ文に合成する。 */
    public String toSemanticQueryText() {
        return java.util.stream.Stream.of(keyword, productName, brand, aspOfferName)
                .filter(s -> s != null && !s.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
    }

    public String getKeyword() {
        return keyword;
    }

    public String getProductName() {
        return productName;
    }

    public String getBrand() {
        return brand;
    }

    public String getAspOfferName() {
        return aspOfferName;
    }

    public String getGenre() {
        return genre;
    }

    public String getSubGenre() {
        return subGenre;
    }

    public String getTargetAgeRange() {
        return targetAgeRange;
    }

    public String getTargetGender() {
        return targetGender;
    }

    public ContentFormat getPostFormat() {
        return postFormat;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Integer getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public String getPurpose() {
        return purpose;
    }

    public static final class Builder {
        private String keyword;
        private String productName;
        private String brand;
        private String aspOfferName;
        private String genre;
        private String subGenre;
        private String targetAgeRange;
        private String targetGender;
        private ContentFormat postFormat;
        private Platform platform;
        private Integer videoDurationSeconds;
        private String purpose;

        public Builder keyword(String v) {
            this.keyword = v;
            return this;
        }

        public Builder productName(String v) {
            this.productName = v;
            return this;
        }

        public Builder brand(String v) {
            this.brand = v;
            return this;
        }

        public Builder aspOfferName(String v) {
            this.aspOfferName = v;
            return this;
        }

        public Builder genre(String v) {
            this.genre = v;
            return this;
        }

        public Builder subGenre(String v) {
            this.subGenre = v;
            return this;
        }

        public Builder targetAgeRange(String v) {
            this.targetAgeRange = v;
            return this;
        }

        public Builder targetGender(String v) {
            this.targetGender = v;
            return this;
        }

        public Builder postFormat(ContentFormat v) {
            this.postFormat = v;
            return this;
        }

        public Builder platform(Platform v) {
            this.platform = v;
            return this;
        }

        public Builder videoDurationSeconds(Integer v) {
            this.videoDurationSeconds = v;
            return this;
        }

        public Builder purpose(String v) {
            this.purpose = v;
            return this;
        }

        public UserSearchCondition build() {
            return new UserSearchCondition(this);
        }
    }
}
