plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":clients:amt-arrangor-client"))
    implementation(project(":data-publisher"))
    implementation(libs.nav.common.job)
    implementation(libs.nav.token.validation.spring)
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(project(":test:test-utils"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}