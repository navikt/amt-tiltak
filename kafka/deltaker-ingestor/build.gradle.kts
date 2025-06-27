plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":common:json"))
    implementation(project(":core"))
    implementation(project(":clients:mulighetsrommet-api-client"))
    implementation(project(":kafka:gjennomforing-ingestor"))
    implementation(libs.nav.amt.lib.models)
    implementation(libs.unleash.client)
    implementation(libs.nav.common.util) // Not in POM
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
}