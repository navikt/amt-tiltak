dependencies {
    api(project(":core"))
    api(project(":clients:amt-person"))
    api(project(":common:auth"))
    api(project(":common:db_utils"))
    api(project(":data-publisher"))
    api(libs.token.validation.spring)
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api(libs.postgresql)
    api(libs.caffeine)
    api(libs.shedlock.spring)
    api(libs.nav.common.job) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation(libs.mock.oauth2.server)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(project(":db-migrations"))
    testImplementation("org.mockito:mockito-core")
    api(libs.mockito.kotlin)
    testImplementation(project(":test:database"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    api("io.micrometer:micrometer-registry-prometheus")
}
