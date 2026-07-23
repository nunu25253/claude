package com.buzzanalysis.application.imageprompt;

import com.buzzanalysis.application.imageprompt.dto.ImagePromptSetDto;
import com.buzzanalysis.domain.carousel.Carousel;
import com.buzzanalysis.domain.carousel.CarouselPage;
import com.buzzanalysis.domain.carousel.CarouselRepository;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.imageprompt.ImagePrompt;
import com.buzzanalysis.domain.imageprompt.ImagePromptSet;
import com.buzzanalysis.domain.imageprompt.ImagePromptSetRepository;
import com.buzzanalysis.domain.imageprompt.ImagePromptSourceType;
import com.buzzanalysis.domain.script.ScriptCut;
import com.buzzanalysis.domain.script.VideoScript;
import com.buzzanalysis.domain.script.VideoScriptRepository;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 「画像生成プロンプト作成」ユースケース（Phase13）。Phase11の動画台本の各カット、または
 * Phase12のカルーセルの各ページの{@code visualDirection}を入力に、画像生成AI向けの具体的な
 * プロンプト文字列をAIが生成する（実際の画像生成は行わない。設計docのセルフレビュー参照）。
 */
@Service
public class ImagePromptGenerationApplicationService {

    private final VideoScriptRepository videoScriptRepository;
    private final CarouselRepository carouselRepository;
    private final AiImagePromptGenerationPort aiImagePromptGenerationPort;
    private final ImagePromptSetRepository imagePromptSetRepository;
    private final UsageQuotaService usageQuotaService;

    public ImagePromptGenerationApplicationService(VideoScriptRepository videoScriptRepository,
                                                     CarouselRepository carouselRepository,
                                                     AiImagePromptGenerationPort aiImagePromptGenerationPort,
                                                     ImagePromptSetRepository imagePromptSetRepository,
                                                     UsageQuotaService usageQuotaService) {
        this.videoScriptRepository = videoScriptRepository;
        this.carouselRepository = carouselRepository;
        this.aiImagePromptGenerationPort = aiImagePromptGenerationPort;
        this.imagePromptSetRepository = imagePromptSetRepository;
        this.usageQuotaService = usageQuotaService;
    }

    @Transactional
    public ImagePromptSetDto generateForScript(UUID scriptId, UUID requestingUserId) {
        if (!usageQuotaService.tryConsume(requestingUserId)) {
            throw new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
                    UsageQuotaService.EXCEEDED_ERROR_CODE);
        }
        VideoScript script = videoScriptRepository.findById(scriptId)
                .orElseThrow(() -> EntityNotFoundException.of("VideoScript", scriptId));
        List<String> directions = script.getCuts().stream().map(ScriptCut::visualDirection).toList();
        return generate(ImagePromptSourceType.VIDEO_SCRIPT, scriptId, directions);
    }

    @Transactional
    public ImagePromptSetDto generateForCarousel(UUID carouselId, UUID requestingUserId) {
        if (!usageQuotaService.tryConsume(requestingUserId)) {
            throw new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
                    UsageQuotaService.EXCEEDED_ERROR_CODE);
        }
        Carousel carousel = carouselRepository.findById(carouselId)
                .orElseThrow(() -> EntityNotFoundException.of("Carousel", carouselId));
        List<String> directions = carousel.getPages().stream().map(CarouselPage::visualDirection).toList();
        return generate(ImagePromptSourceType.CAROUSEL, carouselId, directions);
    }

    private ImagePromptSetDto generate(ImagePromptSourceType sourceType, UUID sourceId, List<String> directions) {
        List<String> generatedPrompts = aiImagePromptGenerationPort.generate(directions);

        List<ImagePrompt> prompts = new ArrayList<>();
        for (int i = 0; i < directions.size(); i++) {
            String generated = i < generatedPrompts.size() ? generatedPrompts.get(i) : null;
            prompts.add(new ImagePrompt(i + 1, directions.get(i), generated));
        }

        ImagePromptSet promptSet = ImagePromptSet.builder()
                .id(UUID.randomUUID())
                .sourceType(sourceType)
                .sourceId(sourceId)
                .prompts(prompts)
                .createdAt(OffsetDateTime.now())
                .build();

        return ImagePromptSetDto.from(imagePromptSetRepository.save(promptSet));
    }

    @Transactional(readOnly = true)
    public List<ImagePromptSetDto> findBySource(ImagePromptSourceType sourceType, UUID sourceId) {
        return imagePromptSetRepository.findBySource(sourceType, sourceId).stream()
                .map(ImagePromptSetDto::from)
                .toList();
    }
}
