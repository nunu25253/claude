package com.buzzanalysis.domain.imageprompt;

/** 生成元の1要素（台本カット/カルーセルページ）に対応する画像生成プロンプト1件（Phase13）。 */
public record ImagePrompt(int index, String originalDirection, String generatedPrompt) {
}
