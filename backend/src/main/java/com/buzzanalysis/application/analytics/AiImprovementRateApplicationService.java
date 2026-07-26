package com.buzzanalysis.application.analytics;

import com.buzzanalysis.application.analytics.dto.AiImprovementRateDto;
import com.buzzanalysis.domain.score.BuzzScoreHistoryEntry;
import com.buzzanalysis.domain.score.BuzzScoreHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 「AIの改善提案は実際に効果があったか」を可視化するユースケース(戦略監査レポート:
 * 「AIの結論を検証する手段がユーザー側に無い」への対応)。初回分析からAIが改善案を提示し、
 * それを踏まえてユーザーが再分析した投稿群について、BuzzScoreが実際に向上したかを集計する。
 * 架空の実績を語らず、蓄積された実データのみに基づいて算出する(サンプルが無ければnullを返す)。
 */
@Service
public class AiImprovementRateApplicationService {

    /** 集計対象とする再分析済み投稿数の上限。 */
    private static final int MAX_SAMPLE_SIZE = 500;

    private final BuzzScoreHistoryRepository buzzScoreHistoryRepository;

    public AiImprovementRateApplicationService(BuzzScoreHistoryRepository buzzScoreHistoryRepository) {
        this.buzzScoreHistoryRepository = buzzScoreHistoryRepository;
    }

    @Transactional(readOnly = true)
    public AiImprovementRateDto compute() {
        List<UUID> postIds = buzzScoreHistoryRepository.findPostIdsWithAtLeastTwoEntries(MAX_SAMPLE_SIZE);

        int improvedCount = 0;
        List<Double> deltas = new ArrayList<>();
        for (UUID postId : postIds) {
            List<BuzzScoreHistoryEntry> entries = buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId);
            if (entries.size() < 2) {
                continue;
            }
            double first = entries.get(0).getTotalScore();
            double latest = entries.get(entries.size() - 1).getTotalScore();
            double delta = latest - first;
            deltas.add(delta);
            if (delta > 0) {
                improvedCount++;
            }
        }

        int sampleSize = deltas.size();
        if (sampleSize == 0) {
            return new AiImprovementRateDto(0, null, null);
        }
        double improvedPercentage = improvedCount * 100.0 / sampleSize;
        double averageDelta = deltas.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        return new AiImprovementRateDto(sampleSize, improvedPercentage, averageDelta);
    }
}
