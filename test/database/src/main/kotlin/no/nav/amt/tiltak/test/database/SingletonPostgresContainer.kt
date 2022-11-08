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

	private val postgresDockerImageName = getPostgresImage()

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

			container.start()

			log.info("Applying database migrations...")
			applyMigrations(createDataSource(container))

			setupShutdownHook()
		}

		return postgresContainer as PostgreSQLContainer<Nothing>
	}

	private fun applyMigrations(dataSource: DataSource) {
		val flyway: Flyway = Flyway.configure()
			.dataSource(dataSource)
			.connectRetries(10)
			.load()

		flyway.clean()
		flyway.migrate()
	}

	private fun createContainer(): PostgreSQLContainer<Nothing> {
		val container = PostgreSQLContainer<Nothing>(DockerImageName.parse(postgresDockerImageName).asCompatibleSubstituteFor("postgres"))
		container.addEnv("TZ", "europe/oslo")
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


	private fun getPostgresImage(): String {
		val digest = when (System.getProperty("os.arch")) {
			"aarch64" -> "@sha256:58ddae4817fc2b7ed43ac43c91f3cf146290379b7b615210c33fa62a03645e70"
			else -> ""
		}
		return "postgres:14-alpine$digest"
	}

}
