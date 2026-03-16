package com.eggtive.spm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication(exclude = {
	org.springframework.ai.model.bedrock.cohere.autoconfigure.BedrockCohereEmbeddingAutoConfiguration.class,
	org.springframework.ai.model.bedrock.titan.autoconfigure.BedrockTitanEmbeddingAutoConfiguration.class
})
@EnableAsync
public class SpmApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpmApplication.class, args);
	}

}

