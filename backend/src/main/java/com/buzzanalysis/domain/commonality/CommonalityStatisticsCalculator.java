package com.buzzanalysis.domain.commonality;

import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 投稿群から共通ハッシュタグ・共通動画時間・共通投稿時間帯・共通コンテンツ形式を決定的に集計する
 * ドメインサービス（AIを使わない統計項目。Phase8）。
 */
public class CommonalityStatisticsCalculator {

    private static final int TOP_HASHTAG_COUNT = 5;

    public List<String> topHashtags(List<PreprocessedPost> posts) {
        Map<String, Long> counts = posts.stream()
                .flatMap(p -> p.hashtags().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_HASHTAG_COUNT)
                .map(Map.Entry::getKey)
                .toList();
    }

    /** 動画を含む投稿の動画時間（秒）の中央値。動画投稿が1件も無い場合はnull。 */
    public Integer medianVideoDurationSeconds(List<PreprocessedPost> posts) {
        List<Integer> durations = posts.stream()
                .map(PreprocessedPost::videoDuration)
                .filter(v -> v != null && v.durationSeconds() != null)
                .map(v -> v.durationSeconds())
                .sorted()
                .toList();
        if (durations.isEmpty()) {
            return null;
        }
        int middle = durations.size() / 2;
        if (durations.size() % 2 == 0) {
            return (durations.get(middle - 1) + durations.get(middle)) / 2;
        }
        return durations.get(middle);
    }

    /** 最も頻出する投稿時間帯（時）。投稿時間情報が無い場合はnull。 */
    public Integer mostCommonPostingHour(List<PreprocessedPost> posts) {
        Map<Integer, Long> counts = posts.stream()
                .map(PreprocessedPost::postingTime)
                .filter(t -> t != null)
                .collect(Collectors.groupingBy(t -> t.hour(), Collectors.counting()));
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** 最も頻出するコンテンツ形式。投稿が1件も無い場合はnull。 */
    public ContentFormat mostCommonContentFormat(List<PreprocessedPost> posts) {
        Map<ContentFormat, Long> counts = posts.stream()
                .map(PreprocessedPost::contentFormat)
                .filter(f -> f != null)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return counts.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
