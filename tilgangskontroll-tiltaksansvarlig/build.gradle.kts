plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core")) // not in POM
    implementation(project(":common:json")) // not in POM
    implementation(project(":navansatt"))
    implementation(project(":common:auth"))
    implementation(project(":common:db_utils"))
    implementation(project(":common:utils"))
    implementation(libs.nav.common.job) // not in POM
    implementation(libs.nav.token.validation.spring) // not in POM
    implementation(libs.shedlock.spring) // not in POM
    implementation("io.micrometer:micrometer-core") // not in POM
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.postgresql:postgresql")

    testImplementation(project(":db-migrations"))
    testImplementation(project(":test:database"))
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}