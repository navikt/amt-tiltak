plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":tilgangskontroll-tiltaksarrangor"))
    implementation(project(":clients:amt-person"))
    implementation(project(":common:auth"))
    implementation(project(":data-publisher"))
    implementation(project(":common:db_utils"))

    implementation(libs.nav.common.job)
    implementation(libs.testcontainers.postgres)
    implementation(libs.nav.token.validation.spring)

    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("io.micrometer:micrometer-core")

    testImplementation(project(":test:database"))
    testImplementation(project(":test:test-utils"))
    testImplementation(libs.nav.mock.oauth2.server)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
}