dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api(libs.postgresql)
    api(libs.nav.common.job) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    api(project(":common:json"))
    api(project(":core"))
    api(project(":clients:mulighetsrommet-api-client"))
    api(project(":kafka:gjennomforing-ingestor"))
    api(project(":clients:amt-person"))
    testImplementation(libs.mockk)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
    testImplementation(project(":test:database"))
    testImplementation(project(":tiltak"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(project(":arrangor"))
}
