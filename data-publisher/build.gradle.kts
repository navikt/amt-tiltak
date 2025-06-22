plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(libs.jackson.module.kotlin)

    // Core and utility modules
    implementation(project(":core"))
    implementation(project(":common:utils"))
    implementation(project(":common:db_utils"))
    implementation(libs.nav.amt.lib.models)

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Shedlock distributed locking
    implementation(libs.shedlock.spring)

    // Kafka modules
    implementation(libs.nav.common.kafka) // or project(":common:kafka") if local
    implementation(project(":kafka:kafka-config"))
    implementation(project(":kafka:kafka-producer"))

    // JSON utilities
    implementation(project(":common:json"))

    // Arrow Kotlin core library
    implementation(libs.arrow.core)

    // Test dependencies
    testImplementation(project(":db-migrations"))
    testImplementation(project(":test:database"))
    testImplementation(project(":test:test-utils"))
    testImplementation(libs.nav.mock.oauth2.server)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    implementation(libs.testcontainers.junit.jupiter)

    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}