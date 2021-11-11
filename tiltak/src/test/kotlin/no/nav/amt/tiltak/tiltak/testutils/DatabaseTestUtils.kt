package no.nav.amt.tiltak.tiltak.testutils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class DatabaseTestUtils {

	companion object {
		private val postgresContainer: PostgreSQLContainer<Nothing> =
			PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

		private var dataSource: HikariDataSource? = null


		fun getDatabase(dataFile: String? = null): NamedParameterJdbcTemplate {
			if (!postgresContainer.isRunning) {
				postgresContainer.start()
			}

			val dataSource = postgresContainer.createDatasource()

			return loadSchema(dataSource, dataFile)

		}

		private fun PostgreSQLContainer<Nothing>.createDatasource(): HikariDataSource {
			return if (dataSource != null) {
				dataSource!!
			} else {
				val config = HikariConfig()
				config.username = this.username
				config.password = this.password
				config.jdbcUrl = this.jdbcUrl
				config.driverClassName = this.driverClassName
				dataSource = HikariDataSource(config)

				dataSource!!
			}


		}

		private fun loadSchema(
			dataSource: HikariDataSource,
			dataFile: String? = null
		): NamedParameterJdbcTemplate {
			val flyway: Flyway = Flyway.configure()
				.dataSource(dataSource)
				.load()

			flyway.clean()
			flyway.migrate()

			if (dataFile != null) {
				val jdbcTemplate = JdbcTemplate(dataSource)
				jdbcTemplate.update(this::class.java.getResource(dataFile).readText())
			}

			return NamedParameterJdbcTemplate(dataSource)
		}
	}


}
