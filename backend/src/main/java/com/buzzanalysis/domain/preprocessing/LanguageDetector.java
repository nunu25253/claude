package com.buzzanalysis.domain.preprocessing;

/**
 * テキストの言語を判定するドメインサービス。将来、より高精度な統計的/AIベースの判定器に
 * 差し替え可能なようインターフェース化している（依存性逆転の原則）。
 */
public interface LanguageDetector {

    Language detect(String text);
}
