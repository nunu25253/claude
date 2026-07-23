package com.buzzanalysis.application.savedanalysis.dto;

import com.buzzanalysis.application.post.dto.AnalysisResultDto;
import com.buzzanalysis.application.post.dto.BuzzScoreDto;
import com.buzzanalysis.application.post.dto.PostDto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 保存済み分析（ブックマーク）一覧・保存APIのレスポンス。postIdだけでなく、フロントエンドが
 * 分析結果画面をそのまま表示できるよう投稿本体・AI分析結果・BuzzScoreを合わせて返す
 * （{@code POST /posts/analyze}のレスポンスと同じ形に揃えている）。
 * 投稿がAI分析未実施の場合は{@code analysis}/{@code buzzScore}がnullになる。
 */
public record SavedAnalysisDetailDto(
        UUID id,
        String note,
        OffsetDateTime createdAt,
        PostDto post,
        AnalysisResultDto analysis,
        BuzzScoreDto buzzScore,
        Double alertThreshold,
        OffsetDateTime alertTriggeredAt
) {
}
