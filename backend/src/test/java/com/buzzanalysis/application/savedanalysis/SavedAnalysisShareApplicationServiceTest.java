package com.buzzanalysis.application.savedanalysis;

import com.buzzanalysis.application.savedanalysis.dto.SavedAnalysisDetailDto;
import com.buzzanalysis.application.savedanalysis.dto.ShareLinkDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisShareLink;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisShareLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** {@link SavedAnalysisShareApplicationService} の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class SavedAnalysisShareApplicationServiceTest {

    @Mock
    private SavedAnalysisRepository savedAnalysisRepository;
    @Mock
    private SavedAnalysisShareLinkRepository shareLinkRepository;
    @Mock
    private SavedAnalysisApplicationService savedAnalysisApplicationService;

    private SavedAnalysisShareApplicationService service;
    private UUID ownerId;
    private UUID savedAnalysisId;
    private SavedAnalysis saved;

    @BeforeEach
    void setUp() {
        service = new SavedAnalysisShareApplicationService(savedAnalysisRepository, shareLinkRepository,
                savedAnalysisApplicationService);
        ownerId = UUID.randomUUID();
        savedAnalysisId = UUID.randomUUID();
        saved = SavedAnalysis.createNew(ownerId, UUID.randomUUID(), null);
    }

    @Test
    void createOrGetActiveLink_createsNewLink_whenNoneExists() {
        when(savedAnalysisRepository.findById(savedAnalysisId)).thenReturn(Optional.of(saved));
        when(shareLinkRepository.findActiveBySavedAnalysisId(savedAnalysisId)).thenReturn(Optional.empty());
        when(shareLinkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShareLinkDto result = service.createOrGetActiveLink(savedAnalysisId, ownerId);

        ArgumentCaptor<SavedAnalysisShareLink> captor = ArgumentCaptor.forClass(SavedAnalysisShareLink.class);
        verify(shareLinkRepository).save(captor.capture());
        assertThat(captor.getValue().getSavedAnalysisId()).isEqualTo(savedAnalysisId);
        assertThat(result.token()).isEqualTo(captor.getValue().getId());
    }

    @Test
    void createOrGetActiveLink_returnsExistingLink_insteadOfCreatingDuplicate() {
        SavedAnalysisShareLink existing = SavedAnalysisShareLink.create(savedAnalysisId, ownerId);
        when(savedAnalysisRepository.findById(savedAnalysisId)).thenReturn(Optional.of(saved));
        when(shareLinkRepository.findActiveBySavedAnalysisId(savedAnalysisId)).thenReturn(Optional.of(existing));

        ShareLinkDto result = service.createOrGetActiveLink(savedAnalysisId, ownerId);

        assertThat(result.token()).isEqualTo(existing.getId());
        verify(shareLinkRepository, never()).save(any());
    }

    @Test
    void createOrGetActiveLink_throwsBusinessRuleViolation_whenRequesterIsNotOwner() {
        UUID otherUserId = UUID.randomUUID();
        when(savedAnalysisRepository.findById(savedAnalysisId)).thenReturn(Optional.of(saved));

        assertThatThrownBy(() -> service.createOrGetActiveLink(savedAnalysisId, otherUserId))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(shareLinkRepository, never()).save(any());
    }

    @Test
    void revoke_marksActiveLinkAsRevoked() {
        SavedAnalysisShareLink existing = SavedAnalysisShareLink.create(savedAnalysisId, ownerId);
        when(savedAnalysisRepository.findById(savedAnalysisId)).thenReturn(Optional.of(saved));
        when(shareLinkRepository.findActiveBySavedAnalysisId(savedAnalysisId)).thenReturn(Optional.of(existing));

        service.revoke(savedAnalysisId, ownerId);

        ArgumentCaptor<SavedAnalysisShareLink> captor = ArgumentCaptor.forClass(SavedAnalysisShareLink.class);
        verify(shareLinkRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void revoke_doesNothing_whenNoActiveLinkExists() {
        when(savedAnalysisRepository.findById(savedAnalysisId)).thenReturn(Optional.of(saved));
        when(shareLinkRepository.findActiveBySavedAnalysisId(savedAnalysisId)).thenReturn(Optional.empty());

        service.revoke(savedAnalysisId, ownerId);

        verify(shareLinkRepository, never()).save(any());
    }

    @Test
    void getSharedAnalysis_returnsAnalysis_whenLinkIsActive() {
        SavedAnalysisShareLink link = SavedAnalysisShareLink.create(savedAnalysisId, ownerId);
        SavedAnalysisDetailDto expected = new SavedAnalysisDetailDto(
                savedAnalysisId, null, OffsetDateTime.now(), null, null, null, null, null);
        when(shareLinkRepository.findById(link.getId())).thenReturn(Optional.of(link));
        when(savedAnalysisApplicationService.getById(savedAnalysisId)).thenReturn(expected);

        SavedAnalysisDetailDto result = service.getSharedAnalysis(link.getId());

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getSharedAnalysis_throwsNotFound_whenLinkIsRevoked() {
        SavedAnalysisShareLink link = SavedAnalysisShareLink.create(savedAnalysisId, ownerId);
        link.revoke(OffsetDateTime.now());
        when(shareLinkRepository.findById(link.getId())).thenReturn(Optional.of(link));

        assertThatThrownBy(() -> service.getSharedAnalysis(link.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getSharedAnalysis_throwsNotFound_whenTokenDoesNotExist() {
        UUID unknownToken = UUID.randomUUID();
        when(shareLinkRepository.findById(unknownToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSharedAnalysis(unknownToken))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
