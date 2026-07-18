package com.buzzanalysis.domain.commonality;

import com.buzzanalysis.domain.preprocessing.ContentFormat;

import java.util.List;

/**
 * 投稿群の共通点分析結果（Phase8）。統計項目（ハッシュタグ・動画時間・投稿時間・コンテンツ形式）は
 * 決定的に算出され、パターン項目（タイトル/フック/CTA/構成/ターゲット）はAIが抽出する。
 */
public final class CommonalityAnalysisResult {

    private final int totalPostCount;
    private final int aiSampleSize;
    private final List<String> commonHashtags;
    private final Integer commonVideoDurationSeconds;
    private final Integer commonPostingHour;
    private final ContentFormat commonContentFormat;
    private final String commonTitlePattern;
    private final String commonHookPattern;
    private final String commonCtaPattern;
    private final String commonStructurePattern;
    private final String commonTargetPattern;

    private CommonalityAnalysisResult(Builder b) {
        this.totalPostCount = b.totalPostCount;
        this.aiSampleSize = b.aiSampleSize;
        this.commonHashtags = b.commonHashtags == null ? List.of() : b.commonHashtags;
        this.commonVideoDurationSeconds = b.commonVideoDurationSeconds;
        this.commonPostingHour = b.commonPostingHour;
        this.commonContentFormat = b.commonContentFormat;
        this.commonTitlePattern = b.commonTitlePattern;
        this.commonHookPattern = b.commonHookPattern;
        this.commonCtaPattern = b.commonCtaPattern;
        this.commonStructurePattern = b.commonStructurePattern;
        this.commonTargetPattern = b.commonTargetPattern;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getTotalPostCount() {
        return totalPostCount;
    }

    public int getAiSampleSize() {
        return aiSampleSize;
    }

    public List<String> getCommonHashtags() {
        return commonHashtags;
    }

    public Integer getCommonVideoDurationSeconds() {
        return commonVideoDurationSeconds;
    }

    public Integer getCommonPostingHour() {
        return commonPostingHour;
    }

    public ContentFormat getCommonContentFormat() {
        return commonContentFormat;
    }

    public String getCommonTitlePattern() {
        return commonTitlePattern;
    }

    public String getCommonHookPattern() {
        return commonHookPattern;
    }

    public String getCommonCtaPattern() {
        return commonCtaPattern;
    }

    public String getCommonStructurePattern() {
        return commonStructurePattern;
    }

    public String getCommonTargetPattern() {
        return commonTargetPattern;
    }

    public static final class Builder {
        private int totalPostCount;
        private int aiSampleSize;
        private List<String> commonHashtags;
        private Integer commonVideoDurationSeconds;
        private Integer commonPostingHour;
        private ContentFormat commonContentFormat;
        private String commonTitlePattern;
        private String commonHookPattern;
        private String commonCtaPattern;
        private String commonStructurePattern;
        private String commonTargetPattern;

        public Builder totalPostCount(int v) {
            this.totalPostCount = v;
            return this;
        }

        public Builder aiSampleSize(int v) {
            this.aiSampleSize = v;
            return this;
        }

        public Builder commonHashtags(List<String> v) {
            this.commonHashtags = v;
            return this;
        }

        public Builder commonVideoDurationSeconds(Integer v) {
            this.commonVideoDurationSeconds = v;
            return this;
        }

        public Builder commonPostingHour(Integer v) {
            this.commonPostingHour = v;
            return this;
        }

        public Builder commonContentFormat(ContentFormat v) {
            this.commonContentFormat = v;
            return this;
        }

        public Builder commonTitlePattern(String v) {
            this.commonTitlePattern = v;
            return this;
        }

        public Builder commonHookPattern(String v) {
            this.commonHookPattern = v;
            return this;
        }

        public Builder commonCtaPattern(String v) {
            this.commonCtaPattern = v;
            return this;
        }

        public Builder commonStructurePattern(String v) {
            this.commonStructurePattern = v;
            return this;
        }

        public Builder commonTargetPattern(String v) {
            this.commonTargetPattern = v;
            return this;
        }

        public CommonalityAnalysisResult build() {
            return new CommonalityAnalysisResult(this);
        }
    }
}
