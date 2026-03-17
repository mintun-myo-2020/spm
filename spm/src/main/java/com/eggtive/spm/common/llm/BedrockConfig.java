package com.eggtive.spm.common.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

/**
 * Creates the Bedrock Runtime client bean when LLM type is "bedrock".
 */
@Configuration
@ConditionalOnProperty(name = "app.llm.type", havingValue = "bedrock")
public class BedrockConfig {

    private static final Logger log = LoggerFactory.getLogger(BedrockConfig.class);

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient(
            @Value("${app.llm.bedrock.region:#{null}}") String region) {
        var builder = BedrockRuntimeClient.builder();
        if (region != null && !region.isBlank()) {
            builder.region(Region.of(region));
        }
        var client = builder.build();
        log.info("Bedrock client created — region: {}", region != null ? region : "default");
        return client;
    }
}
