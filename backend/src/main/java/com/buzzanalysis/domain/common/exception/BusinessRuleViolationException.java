package com.buzzanalysis.domain.common.exception;

/**
 * ドメインルール違反（例: 重複登録、不正な状態遷移）を表す例外。
 * presentation層の GlobalExceptionHandler で 400 Bad Request にマッピングされる。
 */
public class BusinessRuleViolationException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleViolationException(String message) {
        this(message, null);
    }

    /**
     * errorCodeは、フロントエンドがエラーメッセージの文字列比較に頼らず特定の業務ルール違反を
     * 判別するための任意識別子(例: AI利用上限超過時にアップセルCTAを出し分ける等)。
     * 不要な場合はnullでよい。
     */
    public BusinessRuleViolationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
