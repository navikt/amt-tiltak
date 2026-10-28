dependencies {
    api(project(":db-migrations"))
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api(libs.testcontainers)
    api(libs.otj.pg.embedded)
    api(libs.testcontainers.postgresql)
    api("com.zaxxer:HikariCP")
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")
    api(project(":core"))
    api(project(":common:db_utils"))
    api(libs.kotest.assertions.core)
}
