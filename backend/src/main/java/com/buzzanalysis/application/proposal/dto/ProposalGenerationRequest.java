package com.buzzanalysis.application.proposal.dto;

import java.util.List;
import java.util.UUID;

/** 企画生成APIのリクエストDTO（Phase10）。{@code count}省略時は既定20件。 */
public record ProposalGenerationRequest(List<UUID> postIds, Integer count) {
}
