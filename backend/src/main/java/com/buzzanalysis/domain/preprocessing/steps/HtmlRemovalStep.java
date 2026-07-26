package com.buzzanalysis.domain.preprocessing.steps;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * HTMLタグを除去し、主要なHTMLエンティティをデコードする。
 * SNS投稿本文にHTMLタグが含まれることは稀だが、外部連携やRSS経由取り込み等に備えて実装する。
 */
public class HtmlRemovalStep implements TextCleaningStep {

    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

    private static final Map<String, String> ENTITIES = Map.of(
            "&amp;", "&",
            "&lt;", "<",
            "&gt;", ">",
            "&quot;", "\"",
            "&#39;", "'",
            "&nbsp;", " "
    );

    @Override
    public String apply(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String withoutTags = TAG_PATTERN.matcher(text).replaceAll("");
        String decoded = withoutTags;
        for (Map.Entry<String, String> entity : ENTITIES.entrySet()) {
            decoded = decoded.replace(entity.getKey(), entity.getValue());
        }
        return decoded;
    }
}
