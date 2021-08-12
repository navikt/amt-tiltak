package no.nav.amt.tiltak.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

object LocalPostgresDatabase {

    fun createPostgresContainer(): PostgreSQLContainer<Nothing> {
        return PostgreSQLContainer("postgres:12-alpine")
    }

    fun createDataSource(container: PostgreSQLContainer<Nothing>): HikariDataSource {
        val config = HikariConfig()
        config.username = container.username
        config.password = container.password
        config.jdbcUrl = container.jdbcUrl
        config.driverClassName = container.driverClassName
        return HikariDataSource(config)
    }

    fun createJdbcTemplate(container: PostgreSQLContainer<Nothing>): JdbcTemplate {
        return JdbcTemplate(createDataSource(container))
    }

    fun cleanAndMigrate(jdbcTemplate: JdbcTemplate) {
        cleanAndMigrate(jdbcTemplate.dataSource!!)
    }

    fun cleanAndMigrate(dataSource: DataSource) {
        val flyway: Flyway = Flyway.configure()
            .dataSource(dataSource)
            .load()

        flyway.clean()
        flyway.migrate()
    }

}