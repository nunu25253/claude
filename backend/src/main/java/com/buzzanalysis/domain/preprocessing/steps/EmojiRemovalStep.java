package com.buzzanalysis.domain.preprocessing.steps;

import java.util.regex.Pattern;

/**
 * 絵文字を除去する。主要なUnicode絵文字関連ブロック（Emoticons, Misc Symbols and Pictographs,
 * Transport and Map Symbols, Supplemental Symbols and Pictographs, Dingbats,
 * Miscellaneous Symbols, Regional Indicator（国旗）, 結合文字 ZWJ/バリエーションセレクタ等）を
 * コードポイント範囲で網羅的に除去する、実用ベースの正規表現実装。
 *
 * <p><b>既知の限界:</b> Unicodeには新しい絵文字が継続的に追加されるため、本実装は完全網羅を
 * 保証しない。誤検知・漏れが実運用上問題になる場合は {@code emoji-java} 等の専用ライブラリへの
 * 置き換えを検討すること（Phase2設計レビューにて指摘済み）。</p>
 */
public class EmojiRemovalStep implements TextCleaningStep {

    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\\x{1F300}-\\x{1FAFF}" // Misc Symbols and Pictographs, Emoticons, Transport, Supplemental Symbols
                    + "\\x{2600}-\\x{27BF}"    // Miscellaneous Symbols, Dingbats
                    + "\\x{1F1E6}-\\x{1F1FF}"  // Regional Indicator Symbols（国旗）
                    + "\\x{2B00}-\\x{2BFF}"    // Miscellaneous Symbols and Arrows
                    + "\\x{2300}-\\x{23FF}"    // Miscellaneous Technical（時計等の絵文字含む）
                    + "\\x{FE0F}"              // Variation Selector-16
                    + "\\x{200D}]"             // Zero Width Joiner（結合絵文字）
    );

    @Override
    public String apply(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return EMOJI_PATTERN.matcher(text).replaceAll("");
    }
}
