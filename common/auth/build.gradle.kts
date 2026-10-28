dependencies {
    api(libs.token.validation.spring)
    api(libs.nav.common.audit.log)
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api(project(":core"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mock.oauth2.server)
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.assertions.core)
}
