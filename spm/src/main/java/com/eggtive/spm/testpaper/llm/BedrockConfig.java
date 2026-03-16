package com.eggtive.spm.testpaper.llm;

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

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient(
            @Value("${app.extraction.bedrock.region:#{null}}") String region) {
        var builder = BedrockRuntimeClient.builder();
        if (region != null && !region.isBlank()) {
            builder.region(Region.of(region));
        }
        return builder.build();
    }
}
