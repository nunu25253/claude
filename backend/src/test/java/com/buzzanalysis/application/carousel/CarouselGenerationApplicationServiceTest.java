package com.buzzanalysis.application.carousel;

import com.buzzanalysis.application.carousel.dto.CarouselDto;
import com.buzzanalysis.application.carousel.dto.CarouselGenerationRequest;
import com.buzzanalysis.domain.carousel.Carousel;
import com.buzzanalysis.domain.carousel.CarouselPage;
import com.buzzanalysis.domain.carousel.CarouselRepository;
import com.buzzanalysis.domain.carousel.PageRole;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.proposal.ContentProposal;
import com.buzzanalysis.domain.proposal.ContentProposalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarouselGenerationApplicationServiceTest {

    @Mock
    private ContentProposalRepository contentProposalRepository;
    @Mock
    private AiCarouselGenerationPort aiCarouselGenerationPort;
    @Mock
    private CarouselRepository carouselRepository;

    private CarouselGenerationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new CarouselGenerationApplicationService(contentProposalRepository, aiCarouselGenerationPort,
                carouselRepository);
    }

    private ContentProposal sampleProposal(UUID id) {
        return ContentProposal.builder().id(id).generationId(UUID.randomUUID()).sequenceNumber(1)
                .title("企画").createdAt(OffsetDateTime.now()).build();
    }

    @Test
    void generate_throwsNotFound_whenProposalMissing() {
        UUID proposalId = UUID.randomUUID();
        when(contentProposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(new CarouselGenerationRequest(proposalId)))
                .isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(aiCarouselGenerationPort, carouselRepository);
    }

    @Test
    void generate_assignsRolesByPosition_hookFirstCtaLastExplanationMiddle() {
        UUID proposalId = UUID.randomUUID();
        when(contentProposalRepository.findById(proposalId)).thenReturn(Optional.of(sampleProposal(proposalId)));
        AiCarouselGenerationPort.GeneratedCarousel generated = new AiCarouselGenerationPort.GeneratedCarousel(List.of(
                new AiCarouselGenerationPort.GeneratedPage("見出し1", "本文1", "指示1"),
                new AiCarouselGenerationPort.GeneratedPage("見出し2", "本文2", "指示2"),
                new AiCarouselGenerationPort.GeneratedPage("見出し3", "本文3", "指示3"),
                new AiCarouselGenerationPort.GeneratedPage("見出し4", "本文4", "指示4")
        ));
        when(aiCarouselGenerationPort.generate(any())).thenReturn(generated);
        when(carouselRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CarouselDto result = service.generate(new CarouselGenerationRequest(proposalId));

        assertThat(result.pages()).hasSize(4);
        assertThat(result.pages().get(0).role()).isEqualTo(PageRole.HOOK);
        assertThat(result.pages().get(1).role()).isEqualTo(PageRole.EXPLANATION);
        assertThat(result.pages().get(2).role()).isEqualTo(PageRole.EXPLANATION);
        assertThat(result.pages().get(3).role()).isEqualTo(PageRole.CTA);
        assertThat(result.pages().get(0).pageNumber()).isEqualTo(1);
        assertThat(result.pages().get(3).pageNumber()).isEqualTo(4);
    }

    @Test
    void findByProposalId_returnsDtosFromRepository() {
        UUID proposalId = UUID.randomUUID();
        Carousel carousel = Carousel.builder().id(UUID.randomUUID()).proposalId(proposalId)
                .pages(List.of(new CarouselPage(1, PageRole.HOOK, "見出し", "本文", "指示")))
                .createdAt(OffsetDateTime.now()).build();
        when(carouselRepository.findByProposalId(proposalId)).thenReturn(List.of(carousel));

        List<CarouselDto> result = service.findByProposalId(proposalId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).pages().get(0).role()).isEqualTo(PageRole.HOOK);
    }
}
