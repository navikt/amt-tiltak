plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common:auth"))
    implementation(libs.nav.common.job)
    implementation(libs.nav.token.validation.spring)

    testImplementation(project(":test:test-utils"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}