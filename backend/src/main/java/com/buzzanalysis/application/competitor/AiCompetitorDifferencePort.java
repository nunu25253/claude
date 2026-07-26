package com.buzzanalysis.application.competitor;

import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;

/**
 * OpenAI等のLLMを用いた「競合アカウントとの差分説明」を抽象化するポート（Phase9）。
 * 実装はinfrastructure層に置く。APIキー未設定時はinfrastructure実装側でルールベースの
 * フォールバック説明を返すこと。
 */
public interface AiCompetitorDifferencePort {

    /** targetアカウントとcompetitorアカウントの統計を比較し、差分を自然言語で説明する。 */
    String explainDifference(CompetitorStatsDto target, CompetitorStatsDto competitor);
}
