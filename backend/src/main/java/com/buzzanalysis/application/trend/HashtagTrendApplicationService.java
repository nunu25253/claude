package com.buzzanalysis.application.trend;

import com.buzzanalysis.application.trend.dto.TrendHashtagDto;
import com.buzzanalysis.application.trend.dto.TrendPostDto;
import com.buzzanalysis.application.trend.dto.TrendResponseDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code GET /api/v1/trends} 向けのハッシュタグトレンド集計ユースケース。
 * Phase15の{@link TrendAnalysisApplicationService}（AIサマリー付きの永続化されたトレンドレポート）とは
 * 別物として新規実装する。フロントエンドの{@code TrendResponse}型（hashtags + posts）にそのまま
 * 対応するレスポンスを、決定的な統計計算のみで都度算出して返す（永続化しない・AIを使わない）。
 * 直近ウィンドウ/ベースラインウィンドウの比較によるハッシュタグ急上昇検出はPhase15と同じ考え方を
 * 踏襲するが、既存の{@link TrendAnalysisApplicationService}への影響を避けるため実装は独立させている。
 */
@Service
public class HashtagTrendApplicationService {

    private static final int MAX_POSTS_FOR_AGGREGATION = 500;
    private static final int RECENT_WINDOW_DAYS = 7;
    private static final int BASELINE_WINDOW_DAYS = 21;
    private static final int TOP_N_HASHTAGS = 20;
    private static final int TOP_N_POSTS = 20;
    private static final double EMERGING_GROWTH_RATE_PERCENT = 100.0;

    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final BuzzScoreRepository buzzScoreRepository;

    public HashtagTrendApplicationService(PostRepository postRepository,
                                           AnalysisResultRepository analysisResultRepository,
                                           BuzzScoreRepository buzzScoreRepository) {
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.buzzScoreRepository = buzzScoreRepository;
    }

    @Transactional(readOnly = true)
    public TrendResponseDto getTrends(Platform platform, String genre) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime recentStart = now.minusDays(RECENT_WINDOW_DAYS);
        OffsetDateTime baselineStart = recentStart.minusDays(BASELINE_WINDOW_DAYS);

        List<Post> posts = postRepository.search(
                new PostSearchCriteria(null, null, null, platform, 0, MAX_POSTS_FOR_AGGREGATION,
                        "publishedAt", false)).content();

        Map<UUID, String> genreByPostId = new HashMap<>();
        for (Post post : posts) {
            analysisResultRepository.findByPostId(post.getId())
                    .map(AnalysisResult::getGenre)
                    .filter(g -> g != null && !g.isBlank())
                    .ifPresent(g -> genreByPostId.put(post.getId(), g));
        }

        List<Post> recentPosts = new ArrayList<>();
        List<Post> baselinePosts = new ArrayList<>();
        for (Post post : posts) {
            OffsetDateTime publishedAt = post.getPublishedAt();
            if (publishedAt == null || publishedAt.isBefore(baselineStart)) {
                continue;
            }
            if (genre != null && !genre.isBlank()
                    && !genre.equalsIgnoreCase(genreByPostId.get(post.getId()))) {
                continue;
            }
            if (!publishedAt.isBefore(recentStart)) {
                recentPosts.add(post);
            } else {
                baselinePosts.add(post);
            }
        }

        List<TrendHashtagDto> hashtags = buildHashtagTrends(recentPosts, baselinePosts, genreByPostId);

        List<TrendPostDto> topPosts = recentPosts.stream()
                .sorted(Comparator.comparing(this::buzzScoreOrZero).reversed())
                .limit(TOP_N_POSTS)
                .map(post -> TrendPostDto.from(post, genreByPostId.get(post.getId()), buzzScoreValue(post)))
                .toList();

        return new TrendResponseDto(hashtags, topPosts);
    }

    private List<TrendHashtagDto> buildHashtagTrends(List<Post> recentPosts, List<Post> baselinePosts,
                                                       Map<UUID, String> genreByPostId) {
        Map<String, Integer> recentCounts = new HashMap<>();
        Map<String, Map<Platform, Integer>> platformVotesByTag = new HashMap<>();
        Map<String, Map<String, Integer>> genreVotesByTag = new HashMap<>();
        for (Post post : recentPosts) {
            for (String tag : post.getHashtags()) {
                recentCounts.merge(tag, 1, Integer::sum);
                platformVotesByTag.computeIfAbsent(tag, t -> new HashMap<>())
                        .merge(post.getPlatform(), 1, Integer::sum);
                String postGenre = genreByPostId.get(post.getId());
                if (postGenre != null) {
                    genreVotesByTag.computeIfAbsent(tag, t -> new HashMap<>())
                            .merge(postGenre, 1, Integer::sum);
                }
            }
        }

        Map<String, Integer> baselineCounts = new HashMap<>();
        for (Post post : baselinePosts) {
            for (String tag : post.getHashtags()) {
                baselineCounts.merge(tag, 1, Integer::sum);
            }
        }

        List<TrendHashtagDto> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : recentCounts.entrySet()) {
            String tag = entry.getKey();
            int recentCount = entry.getValue();
            int baselineCount = baselineCounts.getOrDefault(tag, 0);
            double growthRate = baselineCount == 0
                    ? EMERGING_GROWTH_RATE_PERCENT
                    : (recentCount - baselineCount) * 100.0 / baselineCount;

            Platform dominantPlatform = mostFrequentKey(platformVotesByTag.get(tag));
            String dominantGenre = mostFrequentKey(genreVotesByTag.get(tag));

            result.add(new TrendHashtagDto(tag, dominantPlatform, dominantGenre, recentCount, growthRate));
        }

        result.sort(Comparator.comparingDouble(TrendHashtagDto::growthRate).reversed()
                .thenComparing(Comparator.comparingInt(TrendHashtagDto::postCount).reversed()));

        return result.size() > TOP_N_HASHTAGS ? result.subList(0, TOP_N_HASHTAGS) : result;
    }

    private <T> T mostFrequentKey(Map<T, Integer> votes) {
        if (votes == null || votes.isEmpty()) {
            return null;
        }
        return votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Double buzzScoreValue(Post post) {
        return buzzScoreRepository.findByPostId(post.getId()).map(BuzzScore::getTotalScore).orElse(null);
    }

    private double buzzScoreOrZero(Post post) {
        Double score = buzzScoreValue(post);
        return score == null ? 0.0 : score;
    }
}
