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
    implementation("org.flywaydb:flyway-database-postgresql")

    implementation(libs.testcontainers.postgres)
    compileOnly(libs.otj.pg.embedded)
    implementation(libs.kotest.assertions.core)
}