dependencies {
    api(project(":core"))
    api(project(":clients:amt-arrangor-client"))
    api(project(":test:test-utils"))
    api(libs.nav.common.job) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api(libs.token.validation.spring)
    api(project(":data-publisher"))
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
