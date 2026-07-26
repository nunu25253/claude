package com.buzzanalysis.application.savedanalysis.dto;

import com.buzzanalysis.domain.savedanalysis.SavedAnalysisShareLink;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 保存済み分析の共有リンク発行APIのレスポンス。tokenはそのまま公開URL({@code /shared/{token}})に使う。 */
public record ShareLinkDto(UUID token, OffsetDateTime createdAt) {
    public static ShareLinkDto from(SavedAnalysisShareLink link) {
        return new ShareLinkDto(link.getId(), link.getCreatedAt());
    }
}
