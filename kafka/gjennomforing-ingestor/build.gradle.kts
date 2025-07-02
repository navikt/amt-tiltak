plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":common:json"))
    implementation(project(":core"))
    implementation(project(":clients:mulighetsrommet-api-client"))
    implementation("org.springframework.boot:spring-boot-starter")
}