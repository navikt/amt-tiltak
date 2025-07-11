plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common:utils"))
    implementation(project(":common:db_utils"))
    implementation(project(":kafka:kafka-config"))
    implementation(project(":kafka:kafka-producer"))
    implementation(project(":common:json"))

    implementation(libs.jackson.module.kotlin)
    implementation(libs.shedlock.spring)
    implementation(libs.nav.amt.lib.models)
    implementation(libs.nav.common.kafka) // or project(":common:kafka") if local
    implementation(libs.arrow.core)

    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    testImplementation(project(":db-migrations"))
    testImplementation(project(":test:database"))
    testImplementation(project(":test:test-utils"))
    testImplementation(libs.nav.mock.oauth2.server)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    implementation(libs.testcontainers.junit.jupiter)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testRuntimeOnly("org.postgresql:postgresql")
}