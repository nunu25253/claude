package com.buzzanalysis.application.report;

/**
 * オブジェクトストレージ（S3互換）へのアップロードを抽象化するポート。実装はinfrastructure層に置く。
 */
public interface StoragePort {

    /**
     * バイト列をストレージに保存し、保存先キーを返す。
     */
    String upload(String key, byte[] content, String contentType);

    /**
     * 保存済みオブジェクトへの一時アクセスURL（署名付きURL等）を発行する。
     */
    String generateAccessUrl(String key);
}
