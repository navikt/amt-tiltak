plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":common:db_utils"))
    implementation(project(":data-publisher"))
    implementation(project(":clients:amt-arrangor-client"))
    implementation(project(":core"))
    implementation(project(":common:utils"))
    implementation(libs.shedlock.spring)
    implementation(libs.nav.common.job)
    implementation(libs.unleash.client)
    implementation(libs.caffeine)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.data:spring-data-jdbc")

    implementation("org.postgresql:postgresql")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation(project(":db-migrations"))
    testImplementation(project(":test:database"))
    testImplementation(libs.nav.token.validation.spring)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotest.extensions.spring)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
}

tasks.test {
    @Suppress("UNNECESSARY_SAFE_CALL")
    jvmArgs?.add(
        "-Dkotest.framework.config.fqn=no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.KotestConfig"
    )
    useJUnitPlatform()
}
