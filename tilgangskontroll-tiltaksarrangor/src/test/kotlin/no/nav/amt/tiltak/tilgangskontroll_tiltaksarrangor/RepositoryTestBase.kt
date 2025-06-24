package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.TestCase
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import

@AutoConfigureJdbc
@Import(TestDataRepository::class, JdbcTestConfiguration::class)
abstract class RepositoryTestBase(
	body: RepositoryTestBase.() -> Unit
) : BehaviorSpec() {

	@Autowired
	lateinit var testRepository: TestDataRepository

	@Autowired
	lateinit var flyway: Flyway

	override suspend fun beforeContainer(testCase: TestCase) {
		flyway.clean()
		flyway.migrate()
	}

	init {
		this.body()
	}

	companion object {

		@Suppress("unused")
		@ServiceConnection
		private val POSTGRES: org.testcontainers.containers.PostgreSQLContainer<*> =
			org.testcontainers.containers.PostgreSQLContainer<Nothing>("postgres:14-alpine")
				.withCommand("postgres", "-c", "wal_level=logical")
	}
}
