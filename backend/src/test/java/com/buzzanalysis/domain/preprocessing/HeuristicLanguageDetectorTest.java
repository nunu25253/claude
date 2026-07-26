package com.buzzanalysis.domain.preprocessing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeuristicLanguageDetectorTest {

    private final HeuristicLanguageDetector detector = new HeuristicLanguageDetector();

    @Test
    void detect_returnsJapanese_whenTextContainsHiraganaOrKatakana() {
        assertThat(detector.detect("今日はいい天気ですね")).isEqualTo(Language.JAPANESE);
        assertThat(detector.detect("ラーメン食べたい")).isEqualTo(Language.JAPANESE);
    }

    @Test
    void detect_returnsEnglish_whenTextIsLatinAlphabetOnly() {
        assertThat(detector.detect("This is a great product review")).isEqualTo(Language.ENGLISH);
    }

    @Test
    void detect_returnsUnknown_forBlankText() {
        assertThat(detector.detect("")).isEqualTo(Language.UNKNOWN);
        assertThat(detector.detect(null)).isEqualTo(Language.UNKNOWN);
    }

    @Test
    void detect_returnsUnknown_whenTextHasNoLetters() {
        assertThat(detector.detect("123 !!! ###")).isEqualTo(Language.UNKNOWN);
    }
}
