plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":common:json"))
    implementation(project(":clients:amt-arrangor-client"))
    implementation(project(":clients:mulighetsrommet-api-client"))
    implementation(project(":application"))
    implementation(project(":test:database"))
    implementation(libs.nav.common.kafka)
    implementation(libs.testcontainers.postgres)
    implementation(libs.testcontainers.kafka)
    implementation(libs.kotest.framework.concurrency)
    implementation(libs.mockwebserver)
    implementation(libs.nav.mock.oauth2.server)
    implementation(libs.kotest.assertions.core)
    implementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation(project(":test:test-utils"))
}