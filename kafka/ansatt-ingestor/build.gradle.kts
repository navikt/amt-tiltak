plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common:json"))
    implementation(project(":clients:amt-arrangor-client"))
    implementation("org.springframework.boot:spring-boot-starter")
}