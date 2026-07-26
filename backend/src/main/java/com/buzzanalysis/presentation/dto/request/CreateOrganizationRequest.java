package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 組織(チーム)作成リクエスト。 */
public record CreateOrganizationRequest(
        @NotBlank @Size(max = 200) String name
) {
}
