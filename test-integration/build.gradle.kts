plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":common:json")) // not in POM
    implementation(project(":clients:amt-arrangor-client")) // not in POM
    implementation(project(":clients:mulighetsrommet-api-client")) // not in POM
    implementation(libs.nav.common.kafka) // not in POM

    implementation(project(":application"))
    implementation(project(":test:database"))
    implementation(libs.testcontainers.base)
    implementation(libs.testcontainers.postgres)
    implementation(libs.testcontainers.kafka)
    implementation(libs.kotest.framework.concurrency)
    implementation(libs.mockwebserver)
    implementation(libs.nav.mock.oauth2.server)
    implementation(libs.kotest.assertions.core)
    implementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation(project(":test:test-utils"))
}