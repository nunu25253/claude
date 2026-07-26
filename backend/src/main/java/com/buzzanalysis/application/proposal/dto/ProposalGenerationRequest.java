package com.buzzanalysis.application.proposal.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/** 企画生成APIのリクエストDTO（Phase10）。{@code count}省略時は既定20件。 */
public record ProposalGenerationRequest(@NotEmpty List<UUID> postIds, Integer count) {
}
