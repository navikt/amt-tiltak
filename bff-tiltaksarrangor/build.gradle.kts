dependencies {
    api(project(":core"))
    api(project(":common:auth"))
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api(libs.token.validation.spring)
}
