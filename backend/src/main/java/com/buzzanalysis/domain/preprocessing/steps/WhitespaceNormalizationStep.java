package com.buzzanalysis.domain.preprocessing.steps;

import java.util.regex.Pattern;

/**
 * 改行・空白を整理する。連続する改行を1つに圧縮し、各行の行頭行末の空白をトリムし、
 * 連続する半角/全角スペースを1つに圧縮する。他のクレンジングステップ（絵文字/URL/HTML除去）で
 * 生じた余分な空白を最後にまとめて整理する目的で、パイプラインの最後に実行することを想定している。
 */
public class WhitespaceNormalizationStep implements TextCleaningStep {

    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\\n{2,}");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("[ 　]{2,}");
    private static final Pattern TRAILING_SPACE_PER_LINE = Pattern.compile("[ \t]+\\n");

    @Override
    public String apply(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = TRAILING_SPACE_PER_LINE.matcher(text).replaceAll("\n");
        result = MULTIPLE_NEWLINES.matcher(result).replaceAll("\n");
        result = MULTIPLE_SPACES.matcher(result).replaceAll(" ");
        return result.strip();
    }
}
