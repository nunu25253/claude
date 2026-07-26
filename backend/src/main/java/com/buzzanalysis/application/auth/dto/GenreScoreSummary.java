package com.buzzanalysis.application.auth.dto;

/**
 * ジャンル別の平均BuzzScore。週次ダイジェストメールの「今週の勝ちジャンル」表示に使う。
 *
 * @param genre        正規化後のジャンルコード({@link com.buzzanalysis.domain.genre.GenreNormalizer}参照)
 * @param averageScore このジャンルに属する投稿群の平均BuzzScore
 */
public record GenreScoreSummary(String genre, double averageScore) {
}
