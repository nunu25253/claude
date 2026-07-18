package com.buzzanalysis.domain.preprocessing;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 投稿本文からメンション（{@code @username}）を抽出する。 */
public class MentionExtractor {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9_.]+)");

    /** {@code rawText}（クレンジング前のテキスト）からメンション一覧を抽出する（出現順、"@"は含まない）。 */
    public List<String> extract(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }
        Matcher matcher = MENTION_PATTERN.matcher(rawText);
        return matcher.results().map(m -> m.group(1)).distinct().toList();
    }
}
