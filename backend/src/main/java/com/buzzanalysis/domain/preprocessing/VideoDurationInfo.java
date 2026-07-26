package com.buzzanalysis.domain.preprocessing;

/**
 * 動画時間解析の結果。動画を含まない投稿の場合は {@code durationSeconds} / {@code category} ともにnull。
 */
public record VideoDurationInfo(Integer durationSeconds, DurationCategory category) {

    public static VideoDurationInfo notApplicable() {
        return new VideoDurationInfo(null, null);
    }
}
