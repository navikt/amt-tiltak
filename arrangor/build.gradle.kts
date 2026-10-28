dependencies {
    api(libs.nav.common.job) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    api(project(":core"))
    api(project(":tilgangskontroll-tiltaksarrangor"))
    api(project(":clients:amt-person"))
    api(project(":common:auth"))
    api(project(":data-publisher"))
    api(libs.token.validation.spring)
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api(libs.postgresql)
    api(project(":common:db_utils"))
    api("io.micrometer:micrometer-core")
    testImplementation(project(":test:database"))
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation(libs.mock.oauth2.server)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    api(project(":test:test-utils"))
    testImplementation(libs.mockk)
}
