package com.eggtive.spm.report.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3ReportStorage implements ReportStorage {

    private static final Logger log = LoggerFactory.getLogger(S3ReportStorage.class);

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;

    public S3ReportStorage(
            S3Client s3,
            S3Presigner presigner,
            @Value("${app.storage.report-s3-bucket}") String bucket) {
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = bucket;
        log.info("S3 report storage configured: bucket={}", bucket);
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(content));
        log.info("Report uploaded to s3://{}/{}", bucket, key);
        return key;
    }

    @Override
    public String generateUrl(String bucket, String key) {
        var presigned = presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(this.bucket)
                                .key(key)
                                .build())
                        .build());
        return presigned.url().toString();
    }

    @Override
    public byte[] readFile(String key) {
        return s3.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()).asByteArray();
    }
}
