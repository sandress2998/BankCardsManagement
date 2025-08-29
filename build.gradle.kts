plugins {
    java
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
}

group = "ru.mephi"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.github.cdimascio:java-dotenv:5.2.2")

    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Spring Database
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // PostgreSQL
    implementation("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:postgresql")
    // H2
    implementation("com.h2database:h2:2.3.232")

    //Liquibase
    //implementation("org.liquibase:liquibase-core:4.33.0")

    //JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.7") // Для JSON-поддержки

    //Swagger (OpenAPI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

openApi {
    apiDocsUrl.set("http://host.docker.internal:8080/v3/api-docs")
    outputDir.set(file("/docs"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
