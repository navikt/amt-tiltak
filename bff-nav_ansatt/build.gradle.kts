dependencies {
    api(project(":core"))
    api(project(":common:auth"))
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api(project(":tilgangskontroll-tiltaksansvarlig"))
    api(libs.unleash)
    api(libs.token.validation.spring)
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core")
    testImplementation(libs.mockito.kotlin)
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation(libs.kotest.assertions.core)
    testImplementation(project(":test:database"))
    testImplementation(libs.mockk)
}
