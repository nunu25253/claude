package com.buzzanalysis.application.imageprompt;

import com.buzzanalysis.application.imageprompt.dto.ImagePromptSetDto;
import com.buzzanalysis.domain.carousel.Carousel;
import com.buzzanalysis.domain.carousel.CarouselPage;
import com.buzzanalysis.domain.carousel.CarouselRepository;
import com.buzzanalysis.domain.carousel.PageRole;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.imageprompt.ImagePromptSetRepository;
import com.buzzanalysis.domain.imageprompt.ImagePromptSourceType;
import com.buzzanalysis.domain.script.ScriptCut;
import com.buzzanalysis.domain.script.VideoScript;
import com.buzzanalysis.domain.script.VideoScriptRepository;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
@MockitoSettings(strictness = Strictness.LENIENT)
class ImagePromptGenerationApplicationServiceTest {

    @Mock
    private VideoScriptRepository videoScriptRepository;
    @Mock
    private CarouselRepository carouselRepository;
    @Mock
    private AiImagePromptGenerationPort aiImagePromptGenerationPort;
    @Mock
    private ImagePromptSetRepository imagePromptSetRepository;
    @Mock
    private UsageQuotaService usageQuotaService;

    private ImagePromptGenerationApplicationService service;
    private UUID requestingUserId;

    @BeforeEach
    void setUp() {
        service = new ImagePromptGenerationApplicationService(videoScriptRepository, carouselRepository,
                aiImagePromptGenerationPort, imagePromptSetRepository, usageQuotaService);
        requestingUserId = UUID.randomUUID();
        when(usageQuotaService.tryConsume(any())).thenReturn(true);
    }

    @Test
    void generateForScript_throwsNotFound_whenScriptMissing() {
        UUID scriptId = UUID.randomUUID();
        when(videoScriptRepository.findById(scriptId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateForScript(scriptId, requestingUserId))
                .isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(aiImagePromptGenerationPort, imagePromptSetRepository);
    }

    @Test
    void generateForScript_throwsBusinessRuleViolation_whenDailyUsageQuotaExceeded() {
        UUID scriptId = UUID.randomUUID();
        when(usageQuotaService.tryConsume(requestingUserId)).thenReturn(false);

        assertThatThrownBy(() -> service.generateForScript(scriptId, requestingUserId))
                .isInstanceOf(BusinessRuleViolationException.class);
        verifyNoInteractions(videoScriptRepository, aiImagePromptGenerationPort, imagePromptSetRepository);
    }

    @Test
    void generateForScript_extractsVisualDirectionsFromCuts_andPersists() {
        UUID scriptId = UUID.randomUUID();
        VideoScript script = VideoScript.builder().id(scriptId).proposalId(UUID.randomUUID())
                .durationSeconds(30)
                .cuts(List.of(
                        new ScriptCut(1, 0, 10, "ナレ1", "テロップ1", "映像指示1"),
                        new ScriptCut(2, 10, 30, "ナレ2", "テロップ2", "映像指示2")))
                .createdAt(OffsetDateTime.now()).build();
        when(videoScriptRepository.findById(scriptId)).thenReturn(Optional.of(script));
        when(aiImagePromptGenerationPort.generate(List.of("映像指示1", "映像指示2")))
                .thenReturn(List.of("プロンプト1", "プロンプト2"));
        when(imagePromptSetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ImagePromptSetDto result = service.generateForScript(scriptId, requestingUserId);

        assertThat(result.sourceType()).isEqualTo(ImagePromptSourceType.VIDEO_SCRIPT);
        assertThat(result.sourceId()).isEqualTo(scriptId);
        assertThat(result.prompts()).hasSize(2);
        assertThat(result.prompts().get(0).generatedPrompt()).isEqualTo("プロンプト1");
        assertThat(result.prompts().get(0).originalDirection()).isEqualTo("映像指示1");
    }

    @Test
    void generateForCarousel_extractsVisualDirectionsFromPages_andPersists() {
        UUID carouselId = UUID.randomUUID();
        Carousel carousel = Carousel.builder().id(carouselId).proposalId(UUID.randomUUID())
                .pages(List.of(
                        new CarouselPage(1, PageRole.HOOK, "見出し1", "本文1", "画像方向1"),
                        new CarouselPage(2, PageRole.CTA, "見出し2", "本文2", "画像方向2")))
                .createdAt(OffsetDateTime.now()).build();
        when(carouselRepository.findById(carouselId)).thenReturn(Optional.of(carousel));
        when(aiImagePromptGenerationPort.generate(List.of("画像方向1", "画像方向2")))
                .thenReturn(List.of("プロンプトA", "プロンプトB"));
        when(imagePromptSetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ImagePromptSetDto result = service.generateForCarousel(carouselId, requestingUserId);

        assertThat(result.sourceType()).isEqualTo(ImagePromptSourceType.CAROUSEL);
        assertThat(result.prompts()).hasSize(2);
        assertThat(result.prompts().get(1).generatedPrompt()).isEqualTo("プロンプトB");
    }

    @Test
    void findBySource_returnsDtosFromRepository() {
        UUID sourceId = UUID.randomUUID();
        when(imagePromptSetRepository.findBySource(ImagePromptSourceType.VIDEO_SCRIPT, sourceId))
                .thenReturn(List.of());

        List<ImagePromptSetDto> result = service.findBySource(ImagePromptSourceType.VIDEO_SCRIPT, sourceId);

        assertThat(result).isEmpty();
    }
}
