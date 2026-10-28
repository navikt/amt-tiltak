dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api(project(":common:json"))
    api(project(":core"))
    api(project(":clients:mulighetsrommet-api-client"))
    api(project(":kafka:gjennomforing-ingestor"))
    api(libs.unleash)
}
