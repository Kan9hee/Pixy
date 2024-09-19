import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"
	kotlin("plugin.jpa") version "1.9.22"
}

group = "han.graduate"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
	all{
		exclude(group="commons-logging", module="commons-logging")
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework:spring-messaging:6.1.11")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("org.webjars:sockjs-client:1.1.2")
	implementation("org.webjars:stomp-websocket:2.3.4")
	implementation(platform("com.google.cloud:libraries-bom:26.1.4"))
	implementation("com.google.cloud:google-cloud-speech:1.24.6")
	implementation("com.google.protobuf:protobuf-kotlin:3.21.12")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("org.reactivestreams:reactive-streams:1.0.4")
	implementation("org.languagetool:language-en:6.4")
	implementation("com.google.cloud:spring-cloud-gcp-starter:5.0.4")
	implementation("com.google.cloud:spring-cloud-gcp-storage:5.0.4")
	implementation("com.google.code.gson:gson:2.10.1")
	implementation("com.jcraft:jsch:0.1.55")
	implementation("software.amazon.awssdk:s3:2.26.12")
	implementation("software.amazon.awssdk:auth:2.26.12")
	implementation(files("libs/ffmpeg-6.1.1-1.5.10.jar"))

	compileOnly("org.projectlombok:lombok")
	runtimeOnly("com.mysql:mysql-connector-j")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	implementation(kotlin("stdlib"))
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

allOpen {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}