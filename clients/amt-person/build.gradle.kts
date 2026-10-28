dependencies {
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api(libs.okhttp.jvm)
    api(libs.nav.common.token.client)
    api(project(":core"))
    api(project(":common:json"))
    api(libs.nav.common.rest)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotest.runner.junit5)
}
