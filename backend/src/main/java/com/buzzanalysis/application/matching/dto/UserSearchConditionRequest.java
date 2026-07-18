package com.buzzanalysis.application.matching.dto;

import com.buzzanalysis.domain.matching.UserSearchCondition;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.preprocessing.ContentFormat;

/** ユーザー条件分析APIのリクエストDTO（Phase6）。すべての項目は任意。 */
public record UserSearchConditionRequest(
        String keyword,
        String productName,
        String brand,
        String aspOfferName,
        String genre,
        String subGenre,
        String targetAgeRange,
        String targetGender,
        ContentFormat postFormat,
        Platform platform,
        Integer videoDurationSeconds,
        String purpose
) {
    public UserSearchCondition toDomain() {
        return UserSearchCondition.builder()
                .keyword(keyword)
                .productName(productName)
                .brand(brand)
                .aspOfferName(aspOfferName)
                .genre(genre)
                .subGenre(subGenre)
                .targetAgeRange(targetAgeRange)
                .targetGender(targetGender)
                .postFormat(postFormat)
                .platform(platform)
                .videoDurationSeconds(videoDurationSeconds)
                .purpose(purpose)
                .build();
    }
}
