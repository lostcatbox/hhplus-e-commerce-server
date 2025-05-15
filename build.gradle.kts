plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    kotlin("plugin.jpa") version "2.1.0"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

fun getGitHash(): String {
    return providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmToolchain(17)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.redisson:redisson-spring-boot-starter:3.25.2")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // DB
    runtimeOnly("com.mysql:mysql-connector-j")

    // Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testImplementation("io.mockk:mockk:1.13.+")  // 버전은 프로젝트에 맞게 조정
    testImplementation("io.kotest:kotest-runner-junit5:5.+")  // BehaviorSpec을 사용할 경우
    testImplementation("io.kotest:kotest-assertions-core:5.+")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("user.timezone", "UTC")
}
