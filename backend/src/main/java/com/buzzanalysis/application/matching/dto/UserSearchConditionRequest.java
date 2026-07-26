package com.buzzanalysis.application.matching.dto;

import com.buzzanalysis.domain.matching.UserSearchCondition;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import jakarta.validation.constraints.Size;

/** ユーザー条件分析APIのリクエストDTO（Phase6）。すべての項目は任意だが、上限は defense-in-depth として設定する。 */
public record UserSearchConditionRequest(
        @Size(max = 200) String keyword,
        @Size(max = 200) String productName,
        @Size(max = 200) String brand,
        @Size(max = 200) String aspOfferName,
        @Size(max = 100) String genre,
        @Size(max = 100) String subGenre,
        @Size(max = 100) String targetAgeRange,
        @Size(max = 100) String targetGender,
        ContentFormat postFormat,
        Platform platform,
        Integer videoDurationSeconds,
        @Size(max = 200) String purpose
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
