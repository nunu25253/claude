package com.buzzanalysis.domain.common.exception;

/**
 * ドメインルール違反（例: 重複登録、不正な状態遷移）を表す例外。
 * presentation層の GlobalExceptionHandler で 400 Bad Request にマッピングされる。
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
