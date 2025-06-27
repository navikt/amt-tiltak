plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.nav.token.validation.spring)
    implementation(libs.nav.common.audit.log)
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(libs.nav.mock.oauth2.server)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}