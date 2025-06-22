plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

springBoot {
    mainClass.set("no.nav.amt.tiltak.application.ApplicationKt")
}

// 'api' is used instead of 'implementation' for some dependencies in order to expose dependencies to consumers.
dependencies {
    implementation(libs.nav.token.validation.spring) // not in POM
    implementation(libs.nav.common.audit.log)  // not in POM

    // Project dependencies
    implementation(project(":bff-tiltaksarrangor"))
    implementation(project(":bff-nav_ansatt"))
    implementation(project(":bff-internal"))
    api(project(":tiltak"))
    api(project(":tilgangskontroll-tiltaksarrangor"))
    api(project(":arrangor"))
    api(project(":clients:amt-person"))
    api(project(":external-api"))
    implementation(project(":db-migrations"))

    api(project(":kafka:kafka-config"))
    implementation(project(":kafka:kafka-producer"))
    api(project(":kafka:arena-acl-ingestor"))
    implementation(project(":kafka:gjennomforing-ingestor"))
    api(project(":kafka:arrangor-ingestor"))
    api(project(":kafka:ansatt-ingestor"))
    implementation(project(":kafka:nav-bruker-ingestor"))
    implementation(project(":kafka:nav-ansatt-ingestor"))
    api(project(":kafka:deltaker-ingestor"))
    implementation(project(":clients:mulighetsrommet-api-client"))

    // External dependencies from version catalog
    implementation(libs.nav.token.client)
    implementation(libs.nav.poao.tilgang.client)
    implementation(libs.nav.common.log)
    implementation(libs.nav.common.job)
    api(libs.unleash.client)

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-test")

    // Database dependencies
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Shedlock
    implementation(libs.shedlock.spring)
    implementation(libs.shedlock.jdbc)

    // Logging
    implementation(libs.logstash.logback.encoder)

    // Micrometer
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.jackson.module.kotlin)

    // Test dependencies
    testImplementation(project(":test:database"))
}