plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common:json"))
    implementation(libs.nav.token.client)
    implementation(libs.nav.common.rest)
    implementation(libs.okhttp)
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(libs.mockwebserver)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}