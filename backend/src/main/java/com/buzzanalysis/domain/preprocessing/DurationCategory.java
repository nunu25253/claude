package com.buzzanalysis.domain.preprocessing;

/** 動画尺のカテゴリ。Phase11の30/60/90秒台本生成の目安に対応する。 */
public enum DurationCategory {
    /** 15秒以下 */
    SHORT,
    /** 16〜60秒 */
    MEDIUM,
    /** 61〜180秒 */
    LONG,
    /** 181秒以上 */
    EXTENDED
}
