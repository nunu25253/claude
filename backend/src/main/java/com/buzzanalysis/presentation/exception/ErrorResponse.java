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
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse ofValidation(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, fieldErrors);
    }
}
