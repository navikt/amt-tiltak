package no.nav.amt.tiltak.tiltaksleverandor.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltaksleverandor.ansatt.repositories.AnsattRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class AnsattRepositoryTest {

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: AnsattRepository

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = AnsattRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/tiltaksleverandor-ansatt.sql")
	}

	@Test
	internal fun `getByPersonligIdent skal returnere null hvis ident ikke finnes`() {
		assertNull(repository.getByPersonligIdent("687432432"))
	}

	@Test
	internal fun `getByPersonligIdent skal returnere ansatt hvis ident finnes`() {
		val ansatt = repository.getByPersonligIdent("123456789") ?: fail("Ansatt er null")

		assertEquals("6321c7dc-6cfb-47b0-b566-32979be5041f", ansatt.id.toString())
		assertEquals("123456789", ansatt.personligIdent)
		assertEquals("Test", ansatt.fornavn)
		assertEquals("Testersen", ansatt.etternavn)
		assertEquals("1234", ansatt.telefonnummer)
		assertEquals("test@test.no", ansatt.epost)
	}


}
