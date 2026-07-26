package com.buzzanalysis.application.trend;

import com.buzzanalysis.domain.trend.TrendItem;

import java.util.List;

/**
 * OpenAI等のLLMを用いた「トレンドサマリー生成」を抽象化するポート（Phase15）。実装はinfrastructure層に置く。
 * トレンド検出自体は決定的な統計計算（{@code TrendAnalysisApplicationService}）で完結しており、
 * 本ポートは検出結果を自然言語で要約するためだけに使う。
 */
public interface AiTrendSummaryPort {

    String summarize(List<TrendItem> items);
}
