package com.buzzanalysis.domain.common.exception;

/**
 * 集約/エンティティがIDで見つからない場合にスローするドメイン例外。
 * presentation層の GlobalExceptionHandler で 404 Not Found にマッピングされる。
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public static EntityNotFoundException of(String entityName, Object id) {
        return new EntityNotFoundException(entityName + " not found: id=" + id);
    }
}
