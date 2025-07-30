plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common:json"))
    implementation(project(":navansatt"))
    implementation(project(":common:auth"))
    implementation(project(":common:db_utils"))
    implementation(project(":common:utils"))
    implementation(libs.nav.common.job)
    implementation(libs.nav.token.validation.spring)
    implementation(libs.shedlock.spring)
    implementation("io.micrometer:micrometer-core")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation(project(":db-migrations"))
    testImplementation(project(":test:database"))
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
}