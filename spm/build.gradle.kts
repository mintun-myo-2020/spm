plugins {
	java
	id("org.springframework.boot") version "4.0.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.eggtive"
version = "0.0.1-SNAPSHOT"
description = "Student Progress Management from Eggtive"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {

	// Spring Boot starters
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

	// Database
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	// Cache
	implementation("com.github.ben-manes.caffeine:caffeine")

	// AWS SDK v2
	implementation(platform("software.amazon.awssdk:bom:2.42.12"))
	implementation("software.amazon.awssdk:ses")
	implementation("software.amazon.awssdk:sns")
	implementation("software.amazon.awssdk:s3")
	implementation("software.amazon.awssdk:textract")

	// -------------------------
	// Testing
	// -------------------------

	// Spring Boot testing framework
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// Security testing
	testImplementation("org.springframework.security:spring-security-test")

	// Testcontainers
	testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.0"))
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")

	// Spring Boot + Testcontainers integration
	testImplementation("org.springframework.boot:spring-boot-testcontainers")

	// JUnit platform launcher
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
	imageName.set("spm-app:latest")
}