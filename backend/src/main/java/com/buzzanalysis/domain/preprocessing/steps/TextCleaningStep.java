package com.buzzanalysis.domain.preprocessing.steps;

/**
 * テキストクレンジングの1ステップを表すStrategyインターフェース。{@code DefaultTextCleaner}が
 * 注入されたStep一覧を順番に適用する。新しいクレンジング処理を追加する場合は、この
 * インターフェースを実装したクラスを1つ追加するだけでよい。
 */
public interface TextCleaningStep {

    /** このテキストに対して変換を適用し、変換後の文字列を返す。 */
    String apply(String text);
}
