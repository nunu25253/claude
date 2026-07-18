package com.buzzanalysis.infrastructure.config;

import com.buzzanalysis.domain.preprocessing.ContentFormatClassifier;
import com.buzzanalysis.domain.preprocessing.DefaultPostPreprocessor;
import com.buzzanalysis.domain.preprocessing.DefaultTextCleaner;
import com.buzzanalysis.domain.preprocessing.HashtagExtractor;
import com.buzzanalysis.domain.preprocessing.HeuristicLanguageDetector;
import com.buzzanalysis.domain.preprocessing.LanguageDetector;
import com.buzzanalysis.domain.preprocessing.MentionExtractor;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PostingTimeAnalyzer;
import com.buzzanalysis.domain.preprocessing.TextCleaner;
import com.buzzanalysis.domain.preprocessing.VideoDurationAnalyzer;
import com.buzzanalysis.domain.preprocessing.steps.EmojiRemovalStep;
import com.buzzanalysis.domain.preprocessing.steps.HtmlRemovalStep;
import com.buzzanalysis.domain.preprocessing.steps.TextCleaningStep;
import com.buzzanalysis.domain.preprocessing.steps.UrlRemovalStep;
import com.buzzanalysis.domain.preprocessing.steps.WhitespaceNormalizationStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Phase2（前処理レイヤー）のドメインサービス（フレームワーク非依存のPOJO）をSpring Beanとして組み立てる。
 * {@link NormalizationConfig} と同じ方針で、ドメイン層自体はSpringに依存させず配線のみをinfrastructure層で行う。
 * テキストクレンジングのステップ順序: HTML除去 → URL除去 → 絵文字除去 → 改行/空白整理
 * （タグ除去を最初に行い、最後に余分な空白をまとめて整理する）。
 */
@Configuration
public class PreprocessingConfig {

    @Bean
    public List<TextCleaningStep> textCleaningSteps() {
        return List.of(
                new HtmlRemovalStep(),
                new UrlRemovalStep(),
                new EmojiRemovalStep(),
                new WhitespaceNormalizationStep()
        );
    }

    @Bean
    public TextCleaner textCleaner(List<TextCleaningStep> textCleaningSteps) {
        return new DefaultTextCleaner(textCleaningSteps);
    }

    @Bean
    public LanguageDetector languageDetector() {
        return new HeuristicLanguageDetector();
    }

    @Bean
    public HashtagExtractor hashtagExtractor() {
        return new HashtagExtractor();
    }

    @Bean
    public MentionExtractor mentionExtractor() {
        return new MentionExtractor();
    }

    @Bean
    public PostingTimeAnalyzer postingTimeAnalyzer() {
        return new PostingTimeAnalyzer();
    }

    @Bean
    public VideoDurationAnalyzer videoDurationAnalyzer() {
        return new VideoDurationAnalyzer();
    }

    @Bean
    public ContentFormatClassifier contentFormatClassifier() {
        return new ContentFormatClassifier();
    }

    @Bean
    public PostPreprocessor postPreprocessor(TextCleaner textCleaner, LanguageDetector languageDetector,
                                              HashtagExtractor hashtagExtractor, MentionExtractor mentionExtractor,
                                              PostingTimeAnalyzer postingTimeAnalyzer,
                                              VideoDurationAnalyzer videoDurationAnalyzer,
                                              ContentFormatClassifier contentFormatClassifier) {
        return new DefaultPostPreprocessor(textCleaner, languageDetector, hashtagExtractor, mentionExtractor,
                postingTimeAnalyzer, videoDurationAnalyzer, contentFormatClassifier);
    }
}
