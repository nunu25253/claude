package com.buzzanalysis.application.proposal;

import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisResultDto;
import com.buzzanalysis.domain.preprocessing.ContentFormat;

import java.util.List;

/**
 * OpenAI等のLLMを用いた「投稿企画生成」を抽象化するポート（Phase10）。実装はinfrastructure層に置く。
 * 呼び出し側が要求した件数({@code count})を上限として返すが、それより少ない件数が返ることもある
 * （AIが返さなかった分を水増しで埋めることはしない）。
 */
public interface AiProposalGenerationPort {

    List<GeneratedProposal> generate(CommonalityAnalysisResultDto commonality, int count);

    /** AIが生成した1件分の企画。{@code recommendedFormat}はAI応答が既知の値にマッチしない場合はnullとなる。 */
    record GeneratedProposal(String title, String hookPattern, String structureSummary, String callToAction,
                              String targetAudience, String genre, ContentFormat recommendedFormat,
                              String reasoning) {
    }
}
