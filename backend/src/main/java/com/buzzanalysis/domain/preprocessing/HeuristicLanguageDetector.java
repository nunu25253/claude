package com.buzzanalysis.domain.preprocessing;

/**
 * ひらがな・カタカナ・漢字・ラテン文字の出現比率に基づくヒューリスティック（規則ベース）言語判定。
 * AIによる推定ではなく、決定的なルールによる分類である。
 *
 * <p><b>既知の限界:</b> ひらがな・カタカナを含まない漢字のみのテキストは日本語と推定するが、
 * 中国語との判別はできない。高精度な判定が必要になった場合は、統計的言語判定ライブラリ
 * （例: Apache Tika langdetect）ベースの実装に差し替えることを推奨する
 * （{@link LanguageDetector} をインターフェース化済みのため差し替えは容易）。</p>
 */
public class HeuristicLanguageDetector implements LanguageDetector {

    @Override
    public Language detect(String text) {
        if (text == null || text.isBlank()) {
            return Language.UNKNOWN;
        }

        int hiraganaKatakanaCount = 0;
        int kanjiCount = 0;
        int latinCount = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isHiraganaOrKatakana(c)) {
                hiraganaKatakanaCount++;
            } else if (isKanji(c)) {
                kanjiCount++;
            } else if (isLatinLetter(c)) {
                latinCount++;
            }
        }

        if (hiraganaKatakanaCount > 0) {
            return Language.JAPANESE;
        }
        if (kanjiCount > 0 && latinCount == 0) {
            return Language.JAPANESE;
        }
        if (latinCount > 0) {
            return Language.ENGLISH;
        }
        if (kanjiCount > 0) {
            return Language.OTHER;
        }
        return Language.UNKNOWN;
    }

    private boolean isHiraganaOrKatakana(char c) {
        // ひらがな: U+3040-U+309F, カタカナ: U+30A0-U+30FF
        return (c >= '぀' && c <= 'ゟ') || (c >= '゠' && c <= 'ヿ');
    }

    private boolean isKanji(char c) {
        // CJK統合漢字: U+4E00-U+9FFF
        return c >= '一' && c <= '鿿';
    }

    private boolean isLatinLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
}
