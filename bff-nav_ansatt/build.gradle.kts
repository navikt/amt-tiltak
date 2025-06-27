plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common:auth"))
    implementation(project(":tilgangskontroll-tiltaksansvarlig"))
    implementation(libs.nav.token.validation.spring)
    implementation(libs.unleash.client)
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(project(":test:database"))
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}