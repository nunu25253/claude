package com.buzzanalysis.application.carousel.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** カルーセル生成APIのリクエストDTO（Phase12）。 */
public record CarouselGenerationRequest(@NotNull UUID proposalId) {
}
