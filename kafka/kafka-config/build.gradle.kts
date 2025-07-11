plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common:json"))
    implementation(libs.nav.common.kafka)
    implementation(libs.shedlock.jdbc)
    implementation("io.micrometer:micrometer-core")
    implementation("org.springframework.boot:spring-boot-starter")

    testImplementation(project(":test:database"))
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly("org.postgresql:postgresql")
}