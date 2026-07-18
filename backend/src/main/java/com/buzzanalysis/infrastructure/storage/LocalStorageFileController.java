package com.buzzanalysis.infrastructure.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link LocalFileStorageService}が保存したファイルを配信する。S3の署名付きURLの代替であり、
 * {@code storage.provider=local}のときのみ有効（SecurityConfigでこのパスは認証不要としている）。
 */
@RestController
@ConditionalOnProperty(prefix = "storage", name = "provider", havingValue = "local", matchIfMissing = true)
@RequestMapping("/api/v1/reports/files")
public class LocalStorageFileController {

    private final LocalFileStorageService localFileStorageService;

    public LocalStorageFileController(LocalFileStorageService localFileStorageService) {
        this.localFileStorageService = localFileStorageService;
    }

    @GetMapping("/{*key}")
    public ResponseEntity<byte[]> download(@PathVariable String key) {
        String normalizedKey = key.startsWith("/") ? key.substring(1) : key;
        byte[] content = localFileStorageService.read(normalizedKey);
        return ResponseEntity.ok().contentType(contentTypeFor(normalizedKey)).body(content);
    }

    private MediaType contentTypeFor(String key) {
        if (key.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        if (key.endsWith(".html")) {
            return MediaType.TEXT_HTML;
        }
        return MediaType.parseMediaType("text/markdown");
    }
}
