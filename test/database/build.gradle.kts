plugins {
    id("default-conventions")
    id("spring-boot-conventions")
}

dependencies {
    implementation(project(":db-migrations"))
    implementation(project(":core"))
    implementation(project(":common:db_utils"))
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("com.zaxxer:HikariCP")
    api("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Test and utility dependencies
    compileOnly(libs.testcontainers.base)
    api(libs.testcontainers.postgres) // DataPublisherServiceTest fails with compileOnly
    compileOnly(libs.otj.pg.embedded)
    implementation(libs.kotest.assertions.core)
}