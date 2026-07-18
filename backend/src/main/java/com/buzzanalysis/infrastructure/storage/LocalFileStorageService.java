package com.buzzanalysis.infrastructure.storage;

import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.common.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.buzzanalysis.application.report.StoragePort;

/**
 * {@link StoragePort} のローカルファイルシステム実装。S3/MinIO等の外部オブジェクトストレージを
 * 用意できない開発環境（Docker Composeを使わないネイティブ実行等）向けの既定実装。
 * {@code storage.provider=s3} を明示した場合は{@link S3StorageService}が代わりに使われる
 * （docker-composeプロファイルでは既定でs3に切り替わる）。
 * 保存したファイルは{@link LocalStorageFileController}が {@code GET /api/v1/reports/files/**} で配信する
 * （S3の署名付きURLと同様、URLの知得自体をアクセス権とみなし認証を要求しない）。
 */
@Service
@ConditionalOnProperty(prefix = "storage", name = "provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements StoragePort {

    private final Path baseDir;
    private final String publicBaseUrl;

    public LocalFileStorageService(@Value("${storage.local.base-dir:./data/reports}") String baseDir,
                                    @Value("${app.base-url:http://localhost:8080}") String publicBaseUrl) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return key;
        } catch (IOException e) {
            throw new ExternalApiException("Failed to write report to local storage: " + key, e);
        }
    }

    @Override
    public String generateAccessUrl(String key) {
        resolve(key);
        return publicBaseUrl + "/api/v1/reports/files/" + key;
    }

    /** 保存済みファイルのバイト列を読み込む（{@link LocalStorageFileController}から使用）。 */
    byte[] read(String key) {
        Path target = resolve(key);
        if (!Files.isRegularFile(target)) {
            throw EntityNotFoundException.of("ReportFile", key);
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new ExternalApiException("Failed to read report from local storage: " + key, e);
        }
    }

    private Path resolve(String key) {
        Path target = baseDir.resolve(key).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Invalid storage key: " + key);
        }
        return target;
    }
}
