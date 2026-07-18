package com.buzzanalysis.infrastructure.storage;

import com.buzzanalysis.application.report.StoragePort;
import com.buzzanalysis.domain.common.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;

/**
 * {@link StoragePort} のS3互換オブジェクトストレージ実装（AWS SDK v2）。
 * {@code storage.s3.endpoint} を設定することでMinIO等にも接続できる。
 */
@Service
public class S3StorageService implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner, S3Properties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(content));
            return key;
        } catch (Exception e) {
            log.error("Failed to upload object to S3: key={}", key, e);
            throw new ExternalApiException("Failed to upload report to object storage: " + key, e);
        }
    }

    @Override
    public String generateAccessUrl(String key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(properties.getPresignedUrlExpirationMinutes()))
                    .getObjectRequest(getRequest)
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key={}", key, e);
            throw new ExternalApiException("Failed to generate download URL: " + key, e);
        }
    }
}
