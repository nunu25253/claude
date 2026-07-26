package com.buzzanalysis.application.event;

import com.buzzanalysis.application.rag.RagIndexingApplicationService;
import com.buzzanalysis.application.rag.dto.RagIndexRequest;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.rag.RagSourceType;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Observerパターンの実装: {@link SavedAnalysisCreatedEvent} を購読し、保存された投稿のAI分析結果を
 * RAG索引へ自動登録する(戦略監査レポート: RAGアシスタントの死んだ機能化を解消)。
 * 索引はユーザーが保存(ブックマーク)した投稿のみを対象とする。全投稿を自動索引するのではなく
 * ユーザーの明示的なキュレーション行為(保存)を起点とすることで、RAGの検索対象を
 * ユーザー自身が「重要」と判断したデータに絞り込める。
 */
@Component
public class SavedAnalysisRagIndexingListener {

    private static final Logger log = LoggerFactory.getLogger(SavedAnalysisRagIndexingListener.class);
    private static final int CONTENT_TEXT_MAX_LENGTH = 20000;

    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final RagIndexingApplicationService ragIndexingApplicationService;

    public SavedAnalysisRagIndexingListener(PostRepository postRepository,
                                             AnalysisResultRepository analysisResultRepository,
                                             RagIndexingApplicationService ragIndexingApplicationService) {
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.ragIndexingApplicationService = ragIndexingApplicationService;
    }

    @Async
    @EventListener
    public void onSavedAnalysisCreated(SavedAnalysisCreatedEvent event) {
        Optional<AnalysisResult> analysisResult = analysisResultRepository.findByPostId(event.getPostId());
        if (analysisResult.isEmpty()) {
            // 未分析の投稿をブックマークしただけの場合は索引対象がないため何もしない。
            return;
        }
        Optional<Post> post = postRepository.findById(event.getPostId());
        if (post.isEmpty()) {
            log.warn("SavedAnalysisCreatedEvent references a post that no longer exists: postId={}", event.getPostId());
            return;
        }

        String contentText = buildContentText(post.get(), analysisResult.get());
        ragIndexingApplicationService.index(
                new RagIndexRequest(RagSourceType.ANALYSIS_RESULT, analysisResult.get().getId(), contentText),
                event.getUserId());
        log.debug("Indexed analysis result {} into RAG for user={}", analysisResult.get().getId(), event.getUserId());
    }

    private String buildContentText(Post post, AnalysisResult analysis) {
        String text = """
                投稿キャプション: %s
                ジャンル: %s / %s
                バズった理由: %s
                ターゲット層: %s
                フック: %s
                CTA: %s
                構成: %s
                強み: %s
                弱み: %s
                改善提案: %s
                """.formatted(
                nullToEmpty(post.getCaption()), nullToEmpty(analysis.getGenre()), nullToEmpty(analysis.getSubGenre()),
                nullToEmpty(analysis.getWhyItWentViral()), nullToEmpty(analysis.getTargetAudience()),
                nullToEmpty(analysis.getHook()), nullToEmpty(analysis.getCallToAction()),
                nullToEmpty(analysis.getPostStructureAnalysis()), nullToEmpty(analysis.getStrengths()),
                nullToEmpty(analysis.getWeaknesses()), nullToEmpty(analysis.getImprovementSuggestions()));
        return text.length() > CONTENT_TEXT_MAX_LENGTH ? text.substring(0, CONTENT_TEXT_MAX_LENGTH) : text;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
