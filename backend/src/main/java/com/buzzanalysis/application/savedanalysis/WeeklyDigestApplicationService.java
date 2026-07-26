package com.buzzanalysis.application.savedanalysis;

import com.buzzanalysis.application.auth.MailSenderPort;
import com.buzzanalysis.application.auth.dto.GenreScoreSummary;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.genre.GenreNormalizer;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreHistoryEntry;
import com.buzzanalysis.domain.score.BuzzScoreHistoryRepository;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 週次AIダイジェストメール配信ユースケース(戦略監査レポートPhase2: 「週次AIダイジェストで定着率を計測開始」)。
 * 保存済み分析を持つユーザーごとに、今週/先週の平均BuzzScore推移とプラットフォーム全体の勝ちジャンルを
 * まとめて通知する。{@link com.buzzanalysis.infrastructure.scheduling.WeeklyDigestScheduler} から
 * 定期的に呼び出される。
 */
@Service
public class WeeklyDigestApplicationService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyDigestApplicationService.class);

    /** プラットフォーム全体の勝ちジャンル算出対象とする直近投稿数の上限。 */
    private static final int MAX_POSTS_FOR_TOP_GENRE_AGGREGATION = 500;
    private static final int TOP_GENRE_COUNT = 3;

    private final SavedAnalysisRepository savedAnalysisRepository;
    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final BuzzScoreRepository buzzScoreRepository;
    private final BuzzScoreHistoryRepository buzzScoreHistoryRepository;
    private final UserRepository userRepository;
    private final GenreNormalizer genreNormalizer;
    private final MailSenderPort mailSenderPort;

    public WeeklyDigestApplicationService(SavedAnalysisRepository savedAnalysisRepository,
                                           PostRepository postRepository,
                                           AnalysisResultRepository analysisResultRepository,
                                           BuzzScoreRepository buzzScoreRepository,
                                           BuzzScoreHistoryRepository buzzScoreHistoryRepository,
                                           UserRepository userRepository,
                                           GenreNormalizer genreNormalizer,
                                           MailSenderPort mailSenderPort) {
        this.savedAnalysisRepository = savedAnalysisRepository;
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.buzzScoreRepository = buzzScoreRepository;
        this.buzzScoreHistoryRepository = buzzScoreHistoryRepository;
        this.userRepository = userRepository;
        this.genreNormalizer = genreNormalizer;
        this.mailSenderPort = mailSenderPort;
    }

    /**
     * 保存済み分析を持つ全ユーザーについて、今週BuzzScoreの動きがあった人にのみダイジェストを送る。
     * 1ユーザーずつ独立して処理し、個別の失敗(ユーザー削除済み等)が他ユーザーへの配信を止めないようにする。
     *
     * @return 実際にダイジェストを送信した件数
     */
    @Transactional(readOnly = true)
    public int sendDigests() {
        List<GenreScoreSummary> topGenres = computeTopGenres();
        int sentCount = 0;
        for (UUID userId : savedAnalysisRepository.findDistinctUserIds()) {
            try {
                if (sendDigestForUser(userId, topGenres)) {
                    sentCount++;
                }
            } catch (Exception e) {
                log.error("Failed to process weekly digest for user {}", userId, e);
            }
        }
        return sentCount;
    }

    private boolean sendDigestForUser(UUID userId, List<GenreScoreSummary> topGenres) {
        User user = userRepository.findById(userId).orElse(null);
        List<SavedAnalysis> savedAnalyses = savedAnalysisRepository.findByUserId(userId);
        if (user == null || savedAnalyses.isEmpty()) {
            return false;
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime weekAgo = now.minusDays(7);
        OffsetDateTime twoWeeksAgo = now.minusDays(14);

        List<Double> thisWeekScores = new ArrayList<>();
        List<Double> lastWeekScores = new ArrayList<>();
        for (SavedAnalysis saved : savedAnalyses) {
            for (BuzzScoreHistoryEntry entry
                    : buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(saved.getPostId())) {
                if (entry.getCalculatedAt().isAfter(weekAgo)) {
                    thisWeekScores.add(entry.getTotalScore());
                } else if (entry.getCalculatedAt().isAfter(twoWeeksAgo)) {
                    lastWeekScores.add(entry.getTotalScore());
                }
            }
        }

        Double thisWeekAverage = average(thisWeekScores);
        if (thisWeekAverage == null) {
            // 今週スコアの動きがなかった(＝活動していない)ユーザーには空虚なダイジェストを送らない。
            return false;
        }
        Double lastWeekAverage = average(lastWeekScores);

        mailSenderPort.sendWeeklyDigestEmail(user.getEmail(), topGenres, thisWeekAverage, lastWeekAverage,
                savedAnalyses.size());
        return true;
    }

    /** 直近投稿群から、正規化後ジャンルごとの平均BuzzScoreが高い順に上位{@value TOP_GENRE_COUNT}件を返す。 */
    private List<GenreScoreSummary> computeTopGenres() {
        List<Post> posts = postRepository.search(
                new PostSearchCriteria(null, null, null, null, 0, MAX_POSTS_FOR_TOP_GENRE_AGGREGATION,
                        "publishedAt", false)
        ).content();
        List<UUID> postIds = posts.stream().map(Post::getId).toList();

        Map<UUID, String> genreByPostId = analysisResultRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(AnalysisResult::getPostId, AnalysisResult::getGenre, (a, b) -> a));
        Map<UUID, Double> scoreByPostId = buzzScoreRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(BuzzScore::getPostId, BuzzScore::getTotalScore, (a, b) -> a));

        Map<String, List<Double>> scoresByGenre = new HashMap<>();
        for (UUID id : postIds) {
            String normalizedGenre = genreNormalizer.normalize(genreByPostId.get(id));
            Double score = scoreByPostId.get(id);
            if (normalizedGenre == null || score == null) {
                continue;
            }
            scoresByGenre.computeIfAbsent(normalizedGenre, key -> new ArrayList<>()).add(score);
        }

        return scoresByGenre.entrySet().stream()
                .map(entry -> new GenreScoreSummary(entry.getKey(),
                        entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElseThrow()))
                .sorted(Comparator.comparingDouble(GenreScoreSummary::averageScore).reversed())
                .limit(TOP_GENRE_COUNT)
                .toList();
    }

    private Double average(List<Double> scores) {
        return scores.isEmpty() ? null : scores.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    }
}
