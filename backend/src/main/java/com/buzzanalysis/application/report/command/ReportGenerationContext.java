package com.buzzanalysis.application.report.command;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.score.BuzzScore;

/**
 * レポート生成Commandの実行に必要な材料をまとめたコンテキスト（Commandパターンのレシーバー相当のデータ）。
 */
public record ReportGenerationContext(Post post, AnalysisResult analysisResult, BuzzScore buzzScore) {
}
