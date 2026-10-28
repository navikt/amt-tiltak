dependencies {
    api(project(":application"))
    api(libs.testcontainers)
    api(libs.testcontainers.postgresql)
    api(libs.testcontainers.kafka)
    api(project(":test:database"))
    api(project(":test:test-utils"))
    api(libs.kotest.assertions.core)
    api(libs.kotest.framework.concurrency)
    api("org.springframework.boot:spring-boot-starter-test")
    api(libs.mockwebserver)
    api(libs.mock.oauth2.server)
}
