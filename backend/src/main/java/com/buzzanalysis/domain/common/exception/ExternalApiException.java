package com.buzzanalysis.domain.common.exception;

/**
 * SNS公式API / OpenAI API等、外部サービス連携時のエラーを表す例外。
 * presentation層の GlobalExceptionHandler で 502 Bad Gateway にマッピングされる。
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
