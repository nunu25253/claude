package com.buzzanalysis.application.script.dto;

import java.util.UUID;

/** 台本生成APIのリクエストDTO（Phase11）。{@code durationSeconds}は30/60/90のいずれかのみ許可。 */
public record ScriptGenerationRequest(UUID proposalId, int durationSeconds) {
}
