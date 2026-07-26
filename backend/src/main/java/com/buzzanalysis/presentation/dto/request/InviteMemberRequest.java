package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 組織へのメンバー招待リクエスト。既存の登録ユーザーをメールアドレスで指定する。 */
public record InviteMemberRequest(
        @NotBlank @Email String email
) {
}
