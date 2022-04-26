package no.nav.amt.tiltak.ansatt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class AnsattRepositoryTest {

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: ArrangorAnsattRepository

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = ArrangorAnsattRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	internal fun `getByPersonligIdent skal returnere null hvis ident ikke finnes`() {
		assertNull(repository.getByPersonligIdent("687432432"))
	}

	@Test
	internal fun `getByPersonligIdent skal returnere ansatt hvis ident finnes`() {
		val ansatt = repository.getByPersonligIdent(ARRANGOR_ANSATT_1.personlig_ident) ?: fail("Ansatt er null")

		assertEquals(ARRANGOR_ANSATT_1.id, ansatt.id)
		assertEquals(ARRANGOR_ANSATT_1.personlig_ident, ansatt.personligIdent)
		assertEquals("Ansatt 1 fornavn", ansatt.fornavn)
		assertEquals("Ansatt 1 etternavn", ansatt.etternavn)
	}


}
