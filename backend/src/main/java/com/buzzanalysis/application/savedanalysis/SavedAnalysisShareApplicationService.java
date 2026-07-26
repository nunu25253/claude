package com.buzzanalysis.application.savedanalysis;

import com.buzzanalysis.application.savedanalysis.dto.SavedAnalysisDetailDto;
import com.buzzanalysis.application.savedanalysis.dto.ShareLinkDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisShareLink;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisShareLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 保存済み分析を未ログインの外部クライアント(代理店の顧客等)に閲覧専用で共有するユースケース
 * (シニアレビュー: 「外部クライアント向けの閲覧専用共有リンクが無い」への対応)。
 * リンクは推測不可能なUUIDをそのままトークンとして扱い、失効(revoke)すれば即座に無効化される。
 */
@Service
public class SavedAnalysisShareApplicationService {

    private final SavedAnalysisRepository savedAnalysisRepository;
    private final SavedAnalysisShareLinkRepository shareLinkRepository;
    private final SavedAnalysisApplicationService savedAnalysisApplicationService;

    public SavedAnalysisShareApplicationService(SavedAnalysisRepository savedAnalysisRepository,
                                                 SavedAnalysisShareLinkRepository shareLinkRepository,
                                                 SavedAnalysisApplicationService savedAnalysisApplicationService) {
        this.savedAnalysisRepository = savedAnalysisRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.savedAnalysisApplicationService = savedAnalysisApplicationService;
    }

    /** 有効なリンクが既にあればそれを返し(1件に統一する)、無ければ新規発行する。所有者本人のみ実行可能。 */
    @Transactional
    public ShareLinkDto createOrGetActiveLink(UUID savedAnalysisId, UUID requestingUserId) {
        requireOwnership(savedAnalysisId, requestingUserId);
        SavedAnalysisShareLink link = shareLinkRepository.findActiveBySavedAnalysisId(savedAnalysisId)
                .orElseGet(() -> shareLinkRepository.save(
                        SavedAnalysisShareLink.create(savedAnalysisId, requestingUserId)));
        return ShareLinkDto.from(link);
    }

    /** 有効なリンクを失効させる。所有者本人のみ実行可能。リンクが無ければ何もしない(冪等)。 */
    @Transactional
    public void revoke(UUID savedAnalysisId, UUID requestingUserId) {
        requireOwnership(savedAnalysisId, requestingUserId);
        shareLinkRepository.findActiveBySavedAnalysisId(savedAnalysisId).ifPresent(link -> {
            link.revoke(OffsetDateTime.now());
            shareLinkRepository.save(link);
        });
    }

    /**
     * 公開トークンから分析結果を取得する(認証不要)。リンクが存在しない・失効済みの場合はいずれも
     * 同じNotFoundとして扱い、「失効した」という事実自体を外部に漏らさない。
     */
    @Transactional(readOnly = true)
    public SavedAnalysisDetailDto getSharedAnalysis(UUID token) {
        SavedAnalysisShareLink link = shareLinkRepository.findById(token)
                .filter(SavedAnalysisShareLink::isActive)
                .orElseThrow(() -> EntityNotFoundException.of("SavedAnalysisShareLink", token));
        return savedAnalysisApplicationService.getById(link.getSavedAnalysisId());
    }

    private void requireOwnership(UUID savedAnalysisId, UUID requestingUserId) {
        SavedAnalysis saved = savedAnalysisRepository.findById(savedAnalysisId)
                .orElseThrow(() -> EntityNotFoundException.of("SavedAnalysis", savedAnalysisId));
        if (!saved.getUserId().equals(requestingUserId)) {
            throw new BusinessRuleViolationException("You are not allowed to manage sharing for this saved analysis");
        }
    }
}
