dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api(project(":common:json"))
    api(project(":core"))
    api(project(":clients:mulighetsrommet-api-client"))
    testImplementation(libs.kotest.assertions.core)
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
}
