plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":common:json")) // not in POM

    api(project(":core"))
    implementation(project(":clients:amt-person"))
    implementation(project(":clients:amt-arrangor-client"))
    implementation(project(":navansatt"))
    implementation(project(":common:auth"))
    implementation(project(":common:db_utils"))
    implementation(project(":common:utils"))
    implementation(project(":data-publisher"))

    implementation(libs.caffeine)
    implementation(libs.shedlock.spring)
    implementation(libs.unleash.client)
    implementation(libs.nav.token.validation.spring)
    implementation(libs.nav.common.job)
    implementation(libs.nav.common.kafka)
    implementation(libs.nav.poao.tilgang.client)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.postgresql:postgresql")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation(libs.nav.mock.oauth2.server)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(project(":db-migrations"))
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(project(":test:database"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation(project(":arrangor"))
    testImplementation(libs.nav.token.validation.spring)
    testImplementation(project(":test:test-utils"))
}