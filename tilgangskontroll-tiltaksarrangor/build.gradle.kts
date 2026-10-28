dependencies {
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api(libs.postgresql)
    api(project(":common:db_utils"))
    api(project(":data-publisher"))
    api(libs.shedlock.spring)
    api(libs.nav.common.job) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    api("io.micrometer:micrometer-registry-prometheus")
    api(project(":clients:amt-arrangor-client"))
    api(project(":core"))
    api(project(":common:utils"))
    api(libs.unleash)
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation(libs.token.validation.spring)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(project(":db-migrations"))
    testImplementation("org.mockito:mockito-core")
    testImplementation(libs.mockito.kotlin)
    testImplementation(project(":test:database"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
}
