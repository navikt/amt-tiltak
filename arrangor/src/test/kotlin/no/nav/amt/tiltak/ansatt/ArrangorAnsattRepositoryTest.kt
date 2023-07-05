package no.nav.amt.tiltak.ansatt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_3
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class ArrangorAnsattRepositoryTest {

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
	internal fun `Sett Sist oppdatert oppdaterer feltet`() {
		val ansattBeforeUpdate = repository.get(ARRANGOR_ANSATT_1.id)
		ansattBeforeUpdate!!.tilgangerSistSynkronisert shouldBe LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.ofHours(1))

		val now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

		repository.setSistOppdatertForAnsatt(ansattBeforeUpdate.id, now)

		val ansattAfterUpdate = repository.get(ARRANGOR_ANSATT_1.id)
		ansattAfterUpdate!!.tilgangerSistSynkronisert.truncatedTo(ChronoUnit.MINUTES) shouldBe now
	}

	@Test
	internal fun `Get siste oppdaterte returnerer riktig`() {
		repository.setSistOppdatertForAnsatt(ARRANGOR_ANSATT_1.id, LocalDateTime.now())

		val toUpdate = repository.getEldsteSistRolleSynkroniserteAnsatte(1)

		toUpdate.size shouldBe 1
		toUpdate[0].id shouldBe ARRANGOR_ANSATT_2.id
	}

	@Test
	internal fun `getEldsteSisteRolleSynkroniserteAnsatte returnerer maks antall`() {
		repository.setSistOppdatertForAnsatt(ARRANGOR_ANSATT_1.id, LocalDateTime.now().minusWeeks(1))
		repository.setSistOppdatertForAnsatt(ARRANGOR_ANSATT_2.id, LocalDateTime.now().minusWeeks(2))
		repository.setSistOppdatertForAnsatt(ARRANGOR_ANSATT_3.id, LocalDateTime.now().minusWeeks(3))

		val toUpdate = repository.getEldsteSistRolleSynkroniserteAnsatte(1)

		toUpdate.size shouldBe 1
		toUpdate.map { it.id } shouldBe listOf(ARRANGOR_ANSATT_3.id)
	}
}

