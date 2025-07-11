plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":kafka:kafka-config"))
    implementation(project(":common:json"))
    implementation(libs.nav.common.kafka)
    implementation("org.springframework.boot:spring-boot-starter")
}