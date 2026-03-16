package com.eggtive.spm.testpaper.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

/**
 * Creates the Bedrock Runtime client bean only when the extraction type is "bedrock".
 *
 * <p>Authentication uses the AWS SDK default credential chain:
 * env vars → ~/.aws/credentials → ECS container role → EC2 instance profile.
 *
 * <p>Region is configurable via {@code app.extraction.bedrock.region} (defaults to
 * {@code AWS_REGION} env var, then us-east-1).
 */
@Configuration
@ConditionalOnProperty(name = "app.extraction.type", havingValue = "bedrock")
public class BedrockConfig {

    private static final Logger log = LoggerFactory.getLogger(BedrockConfig.class);

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient(
            @Value("${app.extraction.bedrock.region:#{null}}") String region) {
        var builder = BedrockRuntimeClient.builder();
        if (region != null && !region.isBlank()) {
            builder.region(Region.of(region));
        }
        var client = builder.build();

        // Log credential source for debugging
        try {
            var creds = software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create().resolveCredentials();
            String accessKeyId = creds.accessKeyId();
            String secretKey = creds.secretAccessKey();
            log.info("AWS credentials resolved");
        } catch (Exception e) {
            log.warn("AWS credentials NOT available — Bedrock calls will fail: {}", e.getMessage());
        }

        return client;
    }
}
