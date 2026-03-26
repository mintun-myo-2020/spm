package com.eggtive.spm.common.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

/**
 * Shared Bedrock Runtime client — created when either the extraction layer
 * or the LLM layer is configured to use Bedrock.
 *
 * <p>The client is region-scoped but model-agnostic; each consumer passes
 * its own model ID per request via {@code InvokeModelRequest.modelId()}.
 *
 * <p>Region resolution: {@code AWS_REGION} env var → {@code app.bedrock.region}
 * property → SDK default.
 */
@Configuration
@ConditionalOnExpression(
    "'${app.extraction.type:stub}' == 'bedrock' or '${app.llm.type:stub}' == 'bedrock'")
public class BedrockClientConfig {

    private static final Logger log = LoggerFactory.getLogger(BedrockClientConfig.class);

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient(
            @Value("${app.bedrock.region:#{null}}") String region) {
        var builder = BedrockRuntimeClient.builder();
        if (region != null && !region.isBlank()) {
            builder.region(Region.of(region));
        }
        log.info("Bedrock client created — region: {}", region != null ? region : "SDK default");
        return builder.build();
    }
}
