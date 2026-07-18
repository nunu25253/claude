package com.buzzanalysis.domain.preprocessing.steps;

import java.util.regex.Pattern;

/** {@code http(s)://} で始まるURLを除去する。 */
public class UrlRemovalStep implements TextCleaningStep {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    @Override
    public String apply(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return URL_PATTERN.matcher(text).replaceAll("");
    }
}
