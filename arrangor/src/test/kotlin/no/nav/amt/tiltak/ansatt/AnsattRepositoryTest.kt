package no.nav.amt.tiltak.ansatt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattGjennomforingTilgangInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime
import java.util.*

class AnsattRepositoryTest {

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: ArrangorAnsattRepository

	lateinit var testRepository: TestDataRepository

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = ArrangorAnsattRepository(NamedParameterJdbcTemplate(dataSource))

		testRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	internal fun `getByPersonligIdent skal returnere null hvis ident ikke finnes`() {
		assertNull(repository.getByPersonligIdent("687432432"))
	}

	@Test
	internal fun `getByPersonligIdent skal returnere ansatt hvis ident finnes`() {
		val ansatt = repository.getByPersonligIdent(ARRANGOR_ANSATT_1.personligIdent) ?: fail("Ansatt er null")

		assertEquals(ARRANGOR_ANSATT_1.id, ansatt.id)
		assertEquals(ARRANGOR_ANSATT_1.personligIdent, ansatt.personligIdent)
		assertEquals("Ansatt 1 fornavn", ansatt.fornavn)
		assertEquals("Ansatt 1 mellomnavn", ansatt.mellomnavn)
		assertEquals("Ansatt 1 etternavn", ansatt.etternavn)
	}

	@Test
	internal fun `getAnsatteForGjennomforing - skal filtrere pa rolle og gjennomforing`() {
		testRepository.insertArrangorAnsattGjennomforingTilgang(
			ArrangorAnsattGjennomforingTilgangInput(
				id = UUID.randomUUID(),
				ansattId = ARRANGOR_ANSATT_2.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigFra = ZonedDateTime.now().minusHours(1),
				gyldigTil = ZonedDateTime.now().plusYears(1)
			)
		)

		val koordinatorer = repository.getAnsatteForGjennomforing(GJENNOMFORING_1.id, ArrangorAnsattRolle.KOORDINATOR)

		koordinatorer.first().id shouldBe ARRANGOR_ANSATT_1.id

		val veiledere = repository.getAnsatteForGjennomforing(GJENNOMFORING_1.id, ArrangorAnsattRolle.VEILEDER)

		veiledere.any { it.id == ARRANGOR_ANSATT_1.id } shouldBe true
		veiledere.any { it.id == ARRANGOR_ANSATT_2.id } shouldBe true
	}


}
