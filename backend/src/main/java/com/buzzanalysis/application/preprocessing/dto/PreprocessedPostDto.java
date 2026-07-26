package com.buzzanalysis.application.preprocessing.dto;

import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.preprocessing.Language;
import com.buzzanalysis.domain.preprocessing.PostingTimeInfo;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import com.buzzanalysis.domain.preprocessing.VideoDurationInfo;

import java.util.List;
import java.util.UUID;

/** {@link PreprocessedPost}（ドメイン）のapplication層向けDTO。 */
public record PreprocessedPostDto(
        UUID postId,
        String cleanText,
        Language language,
        List<String> hashtags,
        List<String> mentions,
        PostingTimeInfo postingTime,
        VideoDurationInfo videoDuration,
        ContentFormat contentFormat
) {
    public static PreprocessedPostDto from(PreprocessedPost preprocessedPost) {
        return new PreprocessedPostDto(
                preprocessedPost.postId(), preprocessedPost.cleanText(), preprocessedPost.language(),
                preprocessedPost.hashtags(), preprocessedPost.mentions(), preprocessedPost.postingTime(),
                preprocessedPost.videoDuration(), preprocessedPost.contentFormat()
        );
    }
}
