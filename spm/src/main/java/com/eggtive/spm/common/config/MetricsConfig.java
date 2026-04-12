package com.eggtive.spm.common.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Value("${app.llm.bedrock.model-id:none}")
    private String llmModelId;

    @Value("${app.extraction.bedrock.model-id:none}")
    private String extractionModelId;

    @Bean
    public MeterBinder appConfigMetrics() {
        return registry -> Gauge.builder("app_config_info", () -> 1)
                .tag("llm_model", llmModelId)
                .tag("extraction_model", extractionModelId)
                .register(registry);
    }
}
