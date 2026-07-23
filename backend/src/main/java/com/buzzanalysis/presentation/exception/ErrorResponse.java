package com.buzzanalysis.presentation.exception;

import java.time.OffsetDateTime;
import java.util.Map;

/** 全APIエラーレスポンスで共通利用するボディ形式。 */
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors,
        String code
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, null, null);
    }

    /**
     * codeは、フロントエンドがメッセージ文字列に頼らず特定の業務ルール違反を判別するための
     * 任意識別子(例: AI_USAGE_QUOTA_EXCEEDED)。
     */
    public static ErrorResponse of(int status, String error, String message, String path, String code) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, null, code);
    }

    public static ErrorResponse ofValidation(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, fieldErrors, null);
    }
}
