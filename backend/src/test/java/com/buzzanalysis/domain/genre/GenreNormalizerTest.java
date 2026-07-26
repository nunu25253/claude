package com.buzzanalysis.domain.genre;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenreNormalizerTest {

    private final GenreNormalizer normalizer = new GenreNormalizer();

    @Test
    void normalize_leavesCanonicalUppercaseCodeUnchanged() {
        assertThat(normalizer.normalize("BEAUTY")).isEqualTo("BEAUTY");
    }

    @Test
    void normalize_convertsJapaneseAiGenreToCanonicalCode() {
        assertThat(normalizer.normalize("美容")).isEqualTo("BEAUTY");
        assertThat(normalizer.normalize("グルメ")).isEqualTo("FOOD");
        assertThat(normalizer.normalize("エンタメ")).isEqualTo("ENTERTAINMENT");
        assertThat(normalizer.normalize("テクノロジー")).isEqualTo("TECH");
        assertThat(normalizer.normalize("フィットネス")).isEqualTo("FITNESS");
    }

    @Test
    void normalize_convertsLowercaseEnglishSeedDataToCanonicalCode() {
        assertThat(normalizer.normalize("technology")).isEqualTo("TECH");
        assertThat(normalizer.normalize("entertainment")).isEqualTo("ENTERTAINMENT");
        assertThat(normalizer.normalize("food")).isEqualTo("FOOD");
    }

    @Test
    void normalize_uppercasesUnknownValues_insteadOfDiscardingThem() {
        assertThat(normalizer.normalize("未知のジャンル")).isEqualTo("未知のジャンル".toUpperCase());
        assertThat(normalizer.normalize("mystery")).isEqualTo("MYSTERY");
    }

    @Test
    void normalize_returnsNull_forNullOrBlankInput() {
        assertThat(normalizer.normalize(null)).isNull();
        assertThat(normalizer.normalize("  ")).isNull();
    }

    @Test
    void matches_returnsTrue_whenEnglishCodeAndJapaneseAiTextRepresentSameGenre() {
        assertThat(normalizer.matches("BEAUTY", "美容")).isTrue();
        assertThat(normalizer.matches("beauty", "美容")).isTrue();
    }

    @Test
    void matches_returnsFalse_whenGenresDiffer() {
        assertThat(normalizer.matches("BEAUTY", "グルメ")).isFalse();
    }

    @Test
    void matches_returnsFalse_whenFilterIsNull() {
        assertThat(normalizer.matches(null, "美容")).isFalse();
    }
}
