package no.nav.amt.tiltak.test.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

object SingletonPostgresContainer {

	private val log = LoggerFactory.getLogger(javaClass)

	private const val POSTGRES_DOCKER_IMAGE_NAME = "postgres:14-alpine"

	private var postgresContainer: PostgreSQLContainer<Nothing>? = null

	private var containerDataSource: DataSource? = null

	fun getDataSource(): DataSource {
		if (containerDataSource == null) {
			containerDataSource = createDataSource(getContainer())
		}

		return containerDataSource!!
	}

	fun getContainer(): PostgreSQLContainer<Nothing> {
		if (postgresContainer == null) {
			log.info("Starting new postgres database...")

			val container = createContainer()
			postgresContainer = container
			// Cloud SQL har wal_level = 'logical' på grunn av flagget cloudsql.logical_decoding i
			// naiserator.yaml. Vi må sette det samme lokalt for at flywaymigrering skal fungere.
			container.withCommand("postgres", "-c", "wal_level=logical")

			container.start()

			log.info("Applying database migrations...")
			applyMigrations(createDataSource(container))

			setupShutdownHook()
		}

		return postgresContainer as PostgreSQLContainer<Nothing>
	}

	private fun applyMigrations(dataSource: DataSource) {
		val flyway: Flyway = Flyway.configure()
			.configuration(
				mapOf(
					// Disable transactional locks in order to support concurrent indexes
					"flyway.postgresql.transactional.lock" to "false"
				)
			)
			.dataSource(dataSource)
			.connectRetries(10)
			.cleanDisabled(false)
			.load()

		flyway.clean()
		flyway.migrate()
	}

	private fun createContainer(): PostgreSQLContainer<Nothing> {
		val container = PostgreSQLContainer<Nothing>(DockerImageName.parse(POSTGRES_DOCKER_IMAGE_NAME).asCompatibleSubstituteFor("postgres"))
		container.addEnv("TZ", "Europe/Oslo")
		return container.waitingFor(HostPortWaitStrategy())
	}

	private fun createDataSource(container: PostgreSQLContainer<Nothing>): DataSource {
		val config = HikariConfig()

		config.jdbcUrl = container.jdbcUrl
		config.username = container.username
		config.password = container.password
		config.maximumPoolSize = 3
		config.minimumIdle = 1

		return HikariDataSource(config)
	}

	private fun setupShutdownHook() {
		Runtime.getRuntime().addShutdownHook(Thread {
			log.info("Shutting down postgres database...")
			postgresContainer?.stop()
		})
	}
}
