plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(libs.caffeine) // not in POM

    implementation(project(":common:db_utils"))
    implementation(project(":data-publisher"))
    implementation(project(":clients:amt-arrangor-client"))
    implementation(project(":core"))
    implementation(project(":common:utils"))
    implementation(libs.shedlock.spring)
    implementation(libs.nav.common.job)
    implementation(libs.unleash.client)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.postgresql:postgresql")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation(project(":db-migrations"))
    testImplementation(project(":test:database"))
    testImplementation(libs.nav.token.validation.spring)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}