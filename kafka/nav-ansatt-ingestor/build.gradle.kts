plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":common:json"))
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter")
}