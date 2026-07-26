package com.buzzanalysis.application.post;

import com.buzzanalysis.application.post.dto.PostScoreComparisonDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.genre.GenreNormalizer;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 「投稿分析結果の相対評価」ユースケース(戦略監査レポート4章: 前回投稿比・同ジャンル平均比バッジ)。
 * 分析結果は履歴化されているのに画面上は今回の1件しか見えず、良し悪しの相対感覚を持てないという
 * 指摘に対応する。永続化はせず、都度計算するオンデマンド分析(Phase4/6/8と同方針)。
 */
@Service
public class PostScoreComparisonApplicationService {

    /** 同ジャンル平均の算出対象とする直近投稿数の上限(CompetitorAnalysisApplicationService等と同方針)。 */
    private static final int MAX_POSTS_FOR_GENRE_AGGREGATION = 500;
    /** 直前投稿を探す際に遡る件数の上限(通常は直近1件で足りるが同時刻投稿等を考慮し余裕を持たせる)。 */
    private static final int PREVIOUS_POST_SEARCH_LIMIT = 5;

    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final BuzzScoreRepository buzzScoreRepository;
    private final GenreNormalizer genreNormalizer;

    public PostScoreComparisonApplicationService(PostRepository postRepository,
                                                  AnalysisResultRepository analysisResultRepository,
                                                  BuzzScoreRepository buzzScoreRepository,
                                                  GenreNormalizer genreNormalizer) {
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.buzzScoreRepository = buzzScoreRepository;
        this.genreNormalizer = genreNormalizer;
    }

    @Transactional(readOnly = true)
    public PostScoreComparisonDto compare(UUID postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> EntityNotFoundException.of("Post", postId));

        Double previousPostScore = findPreviousPostScore(post);

        String genre = analysisResultRepository.findByPostId(postId).map(AnalysisResult::getGenre).orElse(null);
        GenreAggregation aggregation = genre == null
                ? new GenreAggregation(null, 0)
                : aggregateGenreAverage(genre, post);

        return new PostScoreComparisonDto(previousPostScore, aggregation.average(), aggregation.sampleSize());
    }

    private Double findPreviousPostScore(Post post) {
        if (post.getSocialAccountId() == null) {
            return null;
        }
        PostSearchCriteria criteria = new PostSearchCriteria(null, null, post.getSocialAccountId(), null,
                0, PREVIOUS_POST_SEARCH_LIMIT, "publishedAt", false);
        List<Post> recentPosts = postRepository.search(criteria).content();

        Post previous = recentPosts.stream()
                .filter(p -> !p.getId().equals(post.getId()))
                .filter(p -> post.getPublishedAt() == null || p.getPublishedAt() == null
                        || !p.getPublishedAt().isAfter(post.getPublishedAt()))
                .findFirst()
                .orElse(null);
        if (previous == null) {
            return null;
        }
        return buzzScoreRepository.findByPostId(previous.getId()).map(BuzzScore::getTotalScore).orElse(null);
    }

    /** 同一プラットフォーム内の直近投稿群から、正規化後のジャンルが一致する投稿の平均BuzzScoreを求める。 */
    private GenreAggregation aggregateGenreAverage(String targetGenre, Post excludingPost) {
        List<Post> posts = postRepository.search(
                new PostSearchCriteria(null, null, null, excludingPost.getPlatform(),
                        0, MAX_POSTS_FOR_GENRE_AGGREGATION, "publishedAt", false)
        ).content();
        List<UUID> postIds = posts.stream().map(Post::getId).toList();

        Map<UUID, String> genreByPostId = analysisResultRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(AnalysisResult::getPostId, AnalysisResult::getGenre, (a, b) -> a));
        Map<UUID, Double> scoreByPostId = buzzScoreRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(BuzzScore::getPostId, BuzzScore::getTotalScore, (a, b) -> a));

        List<Double> matchingScores = new ArrayList<>();
        for (UUID id : postIds) {
            if (id.equals(excludingPost.getId())) {
                continue;
            }
            if (!genreNormalizer.matches(genreByPostId.get(id), targetGenre)) {
                continue;
            }
            Double score = scoreByPostId.get(id);
            if (score != null) {
                matchingScores.add(score);
            }
        }

        if (matchingScores.isEmpty()) {
            return new GenreAggregation(null, 0);
        }
        double average = matchingScores.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        return new GenreAggregation(average, matchingScores.size());
    }

    private record GenreAggregation(Double average, int sampleSize) {
    }
}
