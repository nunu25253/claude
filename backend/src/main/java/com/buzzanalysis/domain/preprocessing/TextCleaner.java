package com.buzzanalysis.domain.preprocessing;

/** テキストクレンジングのユースケースを表すインターフェース。 */
public interface TextCleaner {

    String clean(String rawText);
}
