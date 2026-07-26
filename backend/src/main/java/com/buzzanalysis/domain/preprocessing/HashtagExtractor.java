package com.buzzanalysis.domain.preprocessing;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 投稿本文からハッシュタグを抽出する。プラットフォームAPIが返す収集時点のメタデータ
 * （{@code NormalizedPost.hashtags}）とは独立に、本文テキストから直接再抽出することで、
 * クレンジング後のテキストとの一貫性を担保する。
 */
public class HashtagExtractor {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_]+)");

    /** {@code rawText}（クレンジング前のテキスト）からハッシュタグ一覧を抽出する（出現順、"#"は含まない）。 */
    public List<String> extract(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }
        Matcher matcher = HASHTAG_PATTERN.matcher(rawText);
        return matcher.results().map(m -> m.group(1)).distinct().toList();
    }
}
