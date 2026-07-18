package com.buzzanalysis.domain.preprocessing;

import com.buzzanalysis.domain.preprocessing.steps.TextCleaningStep;

import java.util.List;
import java.util.Objects;

/**
 * {@link TextCleaner} の実装。注入された {@link TextCleaningStep} 一覧を順番に適用するパイプライン
 * （Strategyパターン）。ステップの順序は呼び出し側（{@code PreprocessingConfig}）が決定する。
 */
public class DefaultTextCleaner implements TextCleaner {

    private final List<TextCleaningStep> steps;

    public DefaultTextCleaner(List<TextCleaningStep> steps) {
        this.steps = Objects.requireNonNull(steps, "steps must not be null");
    }

    @Override
    public String clean(String rawText) {
        if (rawText == null) {
            return "";
        }
        String result = rawText;
        for (TextCleaningStep step : steps) {
            result = step.apply(result);
        }
        return result;
    }
}
