package com.buzzanalysis.infrastructure.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link LocalFileStorageService}が保存したファイルを配信する。S3の署名付きURLの代替であり、
 * {@code storage.provider=local}のときのみ有効。
 * このパス自体はSecurityConfigで認証不要としているが、{@link LocalUrlSigner}によるHMAC署名と
 * 有効期限の検証を必須とすることでアクセス制御している（署名はURLを知っている本人だけが
 * {@link LocalFileStorageService#generateAccessUrl}経由で入手できる）。
 */
@RestController
@ConditionalOnProperty(prefix = "storage", name = "provider", havingValue = "local", matchIfMissing = true)
@RequestMapping("/api/v1/reports/files")
public class LocalStorageFileController {

    private final LocalFileStorageService localFileStorageService;
    private final LocalUrlSigner urlSigner;

    public LocalStorageFileController(LocalFileStorageService localFileStorageService, LocalUrlSigner urlSigner) {
        this.localFileStorageService = localFileStorageService;
        this.urlSigner = urlSigner;
    }

    @GetMapping("/{*key}")
    public ResponseEntity<byte[]> download(@PathVariable String key,
                                            @RequestParam long expires,
                                            @RequestParam String sig) {
        String normalizedKey = key.startsWith("/") ? key.substring(1) : key;
        if (!urlSigner.isValid(normalizedKey, expires, sig)) {
            throw new AccessDeniedException("Invalid or expired download link: " + normalizedKey);
        }
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
