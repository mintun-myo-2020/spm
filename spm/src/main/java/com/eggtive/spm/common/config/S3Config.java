package com.eggtive.spm.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Shared S3 beans, activated when app.storage.type=s3.
 * Uses default credential/region provider chain (env vars, instance profile, etc.).
 */
@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3Config {

    @Bean
    S3Client s3Client() {
        return S3Client.create();
    }

    @Bean
    S3Presigner s3Presigner() {
        return S3Presigner.create();
    }
}
