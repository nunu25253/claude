package com.buzzanalysis.domain.preprocessing;

/** 動画時間（秒）から尺カテゴリを解析する。動画を含まない投稿は {@link VideoDurationInfo#notApplicable()} を返す。 */
public class VideoDurationAnalyzer {

    public VideoDurationInfo analyze(Integer durationSeconds) {
        if (durationSeconds == null) {
            return VideoDurationInfo.notApplicable();
        }
        return new VideoDurationInfo(durationSeconds, categoryOf(durationSeconds));
    }

    private DurationCategory categoryOf(int seconds) {
        if (seconds <= 15) {
            return DurationCategory.SHORT;
        }
        if (seconds <= 60) {
            return DurationCategory.MEDIUM;
        }
        if (seconds <= 180) {
            return DurationCategory.LONG;
        }
        return DurationCategory.EXTENDED;
    }
}
