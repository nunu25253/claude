package com.buzzanalysis.application.post.dto;

import com.buzzanalysis.domain.score.BuzzScoreHistoryEntry;

import java.time.OffsetDateTime;

/** BuzzScore推移グラフ1点分のapplication層向けDTO。 */
public record BuzzScoreHistoryPointDto(OffsetDateTime calculatedAt, double totalScore) {

    public static BuzzScoreHistoryPointDto from(BuzzScoreHistoryEntry entry) {
        return new BuzzScoreHistoryPointDto(entry.getCalculatedAt(), entry.getTotalScore());
    }
}
