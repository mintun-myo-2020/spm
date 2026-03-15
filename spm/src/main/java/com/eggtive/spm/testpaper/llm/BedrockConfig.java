package com.eggtive.spm.testpaper.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

/**
 * Creates the Bedrock Runtime client bean only when the extraction type is "bedrock".
 * Uses default credential/region resolution from the AWS SDK (env vars, instance profile, etc.).
 */
@Configuration
@ConditionalOnProperty(name = "app.extraction.type", havingValue = "bedrock")
public class BedrockConfig {

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        return BedrockRuntimeClient.create();
    }
}
