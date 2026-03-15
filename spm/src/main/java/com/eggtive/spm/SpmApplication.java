package com.eggtive.spm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpmApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpmApplication.class, args);
	}

}
