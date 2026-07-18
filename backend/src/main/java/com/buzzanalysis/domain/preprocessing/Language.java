package com.buzzanalysis.domain.preprocessing;

/**
 * テキストの判定言語。ヒューリスティック（規則ベース）判定の結果であり、AIによる推定ではない
 * （{@link HeuristicLanguageDetector} 参照）。
 */
public enum Language {
    JAPANESE,
    ENGLISH,
    OTHER,
    UNKNOWN
}
