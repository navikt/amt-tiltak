plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":clients:amt-person"))
    implementation(project(":common:auth"))
    implementation(project(":common:db_utils"))
    implementation(project(":data-publisher"))
    implementation(libs.nav.token.validation.spring)
    implementation(libs.caffeine)
    implementation(libs.shedlock.spring)
    implementation(libs.nav.common.job)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation(project(":db-migrations"))
    testImplementation(project(":test:database"))
    testImplementation(libs.nav.mock.oauth2.server)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
}
