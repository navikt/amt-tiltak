plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common:auth"))
    implementation(libs.nav.token.validation.spring)
}