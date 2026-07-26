package com.buzzanalysis.application.script.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** 台本生成APIのリクエストDTO（Phase11）。{@code durationSeconds}は30/60/90のいずれかのみ許可。 */
public record ScriptGenerationRequest(@NotNull UUID proposalId, int durationSeconds) {
}
