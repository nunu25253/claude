package com.buzzanalysis.application.imageprompt;

import java.util.List;

/**
 * OpenAI等のLLMを用いた「画像生成プロンプト作成」を抽象化するポート（Phase13）。実装はinfrastructure層に置く。
 * 入力の{@code visualDirections}と同じ順序・同じ件数の結果を返す（要素ごとに独立して生成し、
 * 個々の失敗は当該要素のみルールベースのフォールバックに切り替える。まとまりとしての検証は不要）。
 */
public interface AiImagePromptGenerationPort {

    List<String> generate(List<String> visualDirections);
}
