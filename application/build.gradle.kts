import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    api(project(":bff-tiltaksarrangor"))
    api(project(":bff-nav_ansatt"))
    api(project(":bff-internal"))
    api(project(":tiltak"))
    api(project(":tilgangskontroll-tiltaksarrangor"))
    api(project(":arrangor"))
    api(project(":clients:amt-person"))
    api(project(":external-api"))
    api(project(":db-migrations"))
    api(project(":kafka:kafka-config"))
    api(project(":kafka:kafka-producer"))
    api(project(":kafka:arena-acl-ingestor"))
    api(project(":kafka:gjennomforing-ingestor"))
    api(project(":kafka:arrangor-ingestor"))
    api(project(":kafka:ansatt-ingestor"))
    api(project(":kafka:nav-bruker-ingestor"))
    api(project(":kafka:nav-ansatt-ingestor"))
    api(project(":kafka:deltaker-ingestor"))
    api(project(":clients:mulighetsrommet-api-client"))
    api(libs.nav.common.token.client)
    api(libs.poao.tilgang.client) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    api(libs.nav.common.log)
    api(libs.nav.common.job) {
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }
    api(libs.unleash)
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api("org.springframework.boot:spring-boot-starter-logging")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-data-jdbc")
    api("org.springframework.boot:spring-boot-starter-validation")
    api(libs.token.validation.spring)
    api(libs.shedlock.jdbc.template)
    api(libs.jackson.module.kotlin)
    api("org.flywaydb:flyway-core")
    api("org.springframework.boot:spring-boot-flyway")
    api("org.flywaydb:flyway-database-postgresql")
    api("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly(libs.postgresql)
    api(libs.logstash.encoder)
    api(libs.shedlock.spring)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(project(":test:database"))
    testImplementation(libs.mockk)
}

springBoot {
    mainClass.set("no.nav.amt.tiltak.application.ApplicationKt")
}

tasks.named<Jar>("jar") {
    enabled = true
    archiveBaseName.set("amt-tiltak")
    archiveClassifier.set("")
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("amt-tiltak")
    archiveClassifier.set("app")
    mainClass.set("no.nav.amt.tiltak.application.ApplicationKt")
}
