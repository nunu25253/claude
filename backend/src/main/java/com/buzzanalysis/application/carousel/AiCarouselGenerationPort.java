package com.buzzanalysis.application.carousel;

import com.buzzanalysis.application.proposal.dto.ContentProposalDto;

import java.util.List;

/**
 * OpenAI等のLLMを用いた「カルーセル生成」を抽象化するポート（Phase12）。実装はinfrastructure層に置く。
 * ページの役割(HOOK/EXPLANATION/CTA)はAIに判定させず、実装側が配列内の位置から決定した上で返す
 * （呼び出し側は追加検証をしない前提）。
 */
public interface AiCarouselGenerationPort {

    GeneratedCarousel generate(ContentProposalDto proposal);

    /** 役割を含まない、AIが生成した1ページ分の内容。役割は呼び出し元（infrastructure実装）が位置から付与する。 */
    record GeneratedPage(String headline, String bodyText, String visualDirection) {
    }

    record GeneratedCarousel(List<GeneratedPage> pages) {
    }
}
