package com.buzzanalysis.domain.genre;

import java.util.Map;
import java.util.Objects;

/**
 * ジャンル文字列を正規化するドメインサービス。
 * <p>
 * ジャンルは複数の異なる形式で保持されている: フロントエンドは英大文字コード(例: {@code BEAUTY})、
 * AI分析結果({@link com.buzzanalysis.domain.analysis.AnalysisResult#getGenre()})は自由記述の日本語
 * (例: "美容")、サンプルデータの{@code rankings.genre}は英小文字(例: "technology")。このままでは
 * 完全一致比較(絞り込みフィルタ等)が実質的に機能しない。本クラスは既知の表記ゆれを
 * フロントエンドの英大文字コードに正規化し、比較・保存の両方で同じ表記に揃えられるようにする。
 * 未知の値は大文字化のみ行い、そのまま返す(未対応ジャンルを{@code OTHER}へ握り潰さないため)。
 */
public class GenreNormalizer {

    private static final Map<String, String> SYNONYMS = Map.ofEntries(
            // 英語表記ゆれ
            Map.entry("TECHNOLOGY", "TECH"),
            Map.entry("GADGET", "TECH"),
            // 日本語(AI分析結果で実際に出現する表記を中心に)
            Map.entry("美容", "BEAUTY"),
            Map.entry("コスメ", "BEAUTY"),
            Map.entry("ファッション", "FASHION"),
            Map.entry("グルメ", "FOOD"),
            Map.entry("フード", "FOOD"),
            Map.entry("料理", "FOOD"),
            Map.entry("旅行", "TRAVEL"),
            Map.entry("トラベル", "TRAVEL"),
            Map.entry("フィットネス", "FITNESS"),
            Map.entry("筋トレ", "FITNESS"),
            Map.entry("エンタメ", "ENTERTAINMENT"),
            Map.entry("エンターテイメント", "ENTERTAINMENT"),
            Map.entry("エンターテインメント", "ENTERTAINMENT"),
            Map.entry("テック", "TECH"),
            Map.entry("テクノロジー", "TECH"),
            Map.entry("ガジェット", "TECH"),
            Map.entry("ライフスタイル", "LIFESTYLE"),
            Map.entry("教育", "EDUCATION"),
            Map.entry("ビジネス", "BUSINESS"),
            Map.entry("その他", "OTHER")
    );

    /**
     * ジャンル文字列を正規化する。既知の表記ゆれはフロントエンドの英大文字コードへ変換し、
     * 未知の値は前後空白除去+大文字化のみ行う。{@code null}/空文字は{@code null}を返す。
     */
    public String normalize(String rawGenre) {
        if (rawGenre == null || rawGenre.isBlank()) {
            return null;
        }
        String trimmed = rawGenre.trim();
        String upper = trimmed.toUpperCase();
        return SYNONYMS.getOrDefault(trimmed, SYNONYMS.getOrDefault(upper, upper));
    }

    /** 2つのジャンル文字列が正規化後に一致するかを判定する。両方{@code null}/空文字なら不一致(false)とする。 */
    public boolean matches(String a, String b) {
        String normalizedA = normalize(a);
        String normalizedB = normalize(b);
        return normalizedA != null && Objects.equals(normalizedA, normalizedB);
    }
}
