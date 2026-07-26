/** プラットフォーム全体の統計情報関連の型定義。 */

export interface AiImprovementRate {
  sampleSize: number;
  /** 集計対象が無い場合はnull(架空の実績を表示しないため)。 */
  improvedPercentage: number | null;
  averageScoreDelta: number | null;
}
