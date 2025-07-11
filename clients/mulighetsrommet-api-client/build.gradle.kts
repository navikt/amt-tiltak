plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":common:json"))
    implementation(libs.okhttp)
    implementation(libs.nav.token.client)
    implementation(libs.nav.common.rest)
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(libs.mockwebserver)
}