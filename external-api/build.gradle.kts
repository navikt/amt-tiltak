dependencies {
    api(project(":core"))
    api(project(":common:auth"))
    api(libs.nav.common.job) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api(libs.token.validation.spring)
    testImplementation(project(":test:test-utils"))
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
