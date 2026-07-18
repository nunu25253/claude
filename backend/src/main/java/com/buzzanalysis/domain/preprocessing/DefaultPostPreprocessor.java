package com.buzzanalysis.domain.preprocessing;

import com.buzzanalysis.domain.normalization.NormalizedPost;

import java.util.Objects;

/**
 * {@link PostPreprocessor} の実装。テキストクレンジング・言語判定・ハッシュタグ/メンション抽出・
 * 投稿時間解析・動画時間解析・投稿タイプ判定の各コラボレーターを組み合わせてオーケストレーションする。
 * フレームワーク非依存の純粋なドメインサービス。
 */
public class DefaultPostPreprocessor implements PostPreprocessor {

    private final TextCleaner textCleaner;
    private final LanguageDetector languageDetector;
    private final HashtagExtractor hashtagExtractor;
    private final MentionExtractor mentionExtractor;
    private final PostingTimeAnalyzer postingTimeAnalyzer;
    private final VideoDurationAnalyzer videoDurationAnalyzer;
    private final ContentFormatClassifier contentFormatClassifier;

    public DefaultPostPreprocessor(TextCleaner textCleaner, LanguageDetector languageDetector,
                                    HashtagExtractor hashtagExtractor, MentionExtractor mentionExtractor,
                                    PostingTimeAnalyzer postingTimeAnalyzer,
                                    VideoDurationAnalyzer videoDurationAnalyzer,
                                    ContentFormatClassifier contentFormatClassifier) {
        this.textCleaner = Objects.requireNonNull(textCleaner);
        this.languageDetector = Objects.requireNonNull(languageDetector);
        this.hashtagExtractor = Objects.requireNonNull(hashtagExtractor);
        this.mentionExtractor = Objects.requireNonNull(mentionExtractor);
        this.postingTimeAnalyzer = Objects.requireNonNull(postingTimeAnalyzer);
        this.videoDurationAnalyzer = Objects.requireNonNull(videoDurationAnalyzer);
        this.contentFormatClassifier = Objects.requireNonNull(contentFormatClassifier);
    }

    @Override
    public PreprocessedPost preprocess(NormalizedPost normalizedPost) {
        String rawText = normalizedPost.rawText();
        String cleanText = textCleaner.clean(rawText);

        return PreprocessedPost.builder()
                .postId(normalizedPost.postId())
                .cleanText(cleanText)
                .language(languageDetector.detect(cleanText))
                .hashtags(hashtagExtractor.extract(rawText))
                .mentions(mentionExtractor.extract(rawText))
                .postingTime(postingTimeAnalyzer.analyze(normalizedPost.publishedAt()))
                .videoDuration(videoDurationAnalyzer.analyze(normalizedPost.videoDurationSeconds()))
                .contentFormat(contentFormatClassifier.classify(normalizedPost))
                .build();
    }
}
