package com.buzzanalysis.application.carousel;

import com.buzzanalysis.application.carousel.dto.CarouselDto;
import com.buzzanalysis.application.carousel.dto.CarouselGenerationRequest;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.domain.carousel.Carousel;
import com.buzzanalysis.domain.carousel.CarouselPage;
import com.buzzanalysis.domain.carousel.CarouselRepository;
import com.buzzanalysis.domain.carousel.PageRole;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.proposal.ContentProposal;
import com.buzzanalysis.domain.proposal.ContentProposalRepository;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 「カルーセル生成」ユースケース（Phase12）。Phase10の投稿企画を入力に、AIがInstagramカルーセル
 * （2〜8ページ）を生成し、永続化する。各ページの役割(HOOK/EXPLANATION/CTA)はAIに判定させず、
 * 配列内の位置（先頭=HOOK、末尾=CTA、それ以外=EXPLANATION）から本サービスが機械的に決定する。
 */
@Service
public class CarouselGenerationApplicationService {

    private final ContentProposalRepository contentProposalRepository;
    private final AiCarouselGenerationPort aiCarouselGenerationPort;
    private final CarouselRepository carouselRepository;
    private final UsageQuotaService usageQuotaService;

    public CarouselGenerationApplicationService(ContentProposalRepository contentProposalRepository,
                                                 AiCarouselGenerationPort aiCarouselGenerationPort,
                                                 CarouselRepository carouselRepository,
                                                 UsageQuotaService usageQuotaService) {
        this.contentProposalRepository = contentProposalRepository;
        this.aiCarouselGenerationPort = aiCarouselGenerationPort;
        this.carouselRepository = carouselRepository;
        this.usageQuotaService = usageQuotaService;
    }

    @Transactional
    public CarouselDto generate(CarouselGenerationRequest request, UUID requestingUserId) {
        if (!usageQuotaService.tryConsume(requestingUserId)) {
            throw new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
                    UsageQuotaService.EXCEEDED_ERROR_CODE);
        }
        ContentProposal proposal = contentProposalRepository.findById(request.proposalId())
                .orElseThrow(() -> EntityNotFoundException.of("ContentProposal", request.proposalId()));

        AiCarouselGenerationPort.GeneratedCarousel generated =
                aiCarouselGenerationPort.generate(ContentProposalDto.from(proposal));

        List<CarouselPage> pages = assignRoles(generated.pages());

        Carousel carousel = Carousel.builder()
                .id(UUID.randomUUID())
                .proposalId(request.proposalId())
                .pages(pages)
                .createdAt(OffsetDateTime.now())
                .build();

        return CarouselDto.from(carouselRepository.save(carousel));
    }

    @Transactional(readOnly = true)
    public List<CarouselDto> findByProposalId(UUID proposalId) {
        return carouselRepository.findByProposalId(proposalId).stream()
                .map(CarouselDto::from)
                .toList();
    }

    private List<CarouselPage> assignRoles(List<AiCarouselGenerationPort.GeneratedPage> generatedPages) {
        List<CarouselPage> pages = new ArrayList<>();
        int lastIndex = generatedPages.size() - 1;
        for (int i = 0; i < generatedPages.size(); i++) {
            AiCarouselGenerationPort.GeneratedPage g = generatedPages.get(i);
            PageRole role = i == 0 ? PageRole.HOOK : (i == lastIndex ? PageRole.CTA : PageRole.EXPLANATION);
            pages.add(new CarouselPage(i + 1, role, g.headline(), g.bodyText(), g.visualDirection()));
        }
        return pages;
    }
}
