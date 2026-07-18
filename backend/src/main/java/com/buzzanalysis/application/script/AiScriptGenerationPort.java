package com.buzzanalysis.application.script;

import com.buzzanalysis.application.proposal.dto.ContentProposalDto;

import java.util.List;

/**
 * OpenAI等のLLMを用いた「台本生成」を抽象化するポート（Phase11）。実装はinfrastructure層に置く。
 * カットの秒数範囲は実装側で{@code durationSeconds}に収まるよう検証・補正した上で返す
 * （呼び出し側は追加検証をしない前提）。
 */
public interface AiScriptGenerationPort {

    GeneratedScript generate(ContentProposalDto proposal, int durationSeconds);

    record GeneratedCut(int cutNumber, int startSecond, int endSecond, String narration, String telop,
                         String visualDirection) {
    }

    record GeneratedScript(String bgmImage, String callToAction, List<GeneratedCut> cuts) {
    }
}
