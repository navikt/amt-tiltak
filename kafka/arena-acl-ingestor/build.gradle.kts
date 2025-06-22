plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":common:json"))
    implementation(project(":core"))
    implementation(project(":clients:mulighetsrommet-api-client"))
    implementation(project(":kafka:gjennomforing-ingestor"))
    implementation(project(":clients:amt-person"))
    implementation(libs.nav.common.job)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.postgresql:postgresql")

    testImplementation(project(":test:database"))
    testImplementation(project(":tiltak"))
    testImplementation(project(":arrangor"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
}