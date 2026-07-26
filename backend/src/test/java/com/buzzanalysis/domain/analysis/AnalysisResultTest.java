package com.buzzanalysis.domain.analysis;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** {@link AnalysisResult}（Builderパターン）の単体テスト。Phase5で追加されたフィールドを含めて検証する。 */
class AnalysisResultTest {

    @Test
    void builder_buildsResultWithAllPhase5Fields() {
        UUID postId = UUID.randomUUID();

        AnalysisResult result = AnalysisResult.builder()
                .postId(postId)
                .genre("美容")
                .subGenre("スキンケア")
                .whyItWentViral("理由")
                .targetAudience("ターゲット")
                .postPurpose("認知獲得")
                .hook("フック")
                .callToAction("CTA")
                .postStructureAnalysis("構成")
                .sentimentAnalysis("感情")
                .strengths("強み")
                .weaknesses("弱み")
                .improvementSuggestions("改善案")
                .build();

        assertThat(result.getPostId()).isEqualTo(postId);
        assertThat(result.getGenre()).isEqualTo("美容");
        assertThat(result.getSubGenre()).isEqualTo("スキンケア");
        assertThat(result.getPostPurpose()).isEqualTo("認知獲得");
        assertThat(result.getPostStructureAnalysis()).isEqualTo("構成");
        assertThat(result.getStrengths()).isEqualTo("強み");
        assertThat(result.getWeaknesses()).isEqualTo("弱み");
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void builder_requiresPostId() {
        assertThatThrownBy(() -> AnalysisResult.builder().genre("美容").build())
                .isInstanceOf(NullPointerException.class);
    }
}
