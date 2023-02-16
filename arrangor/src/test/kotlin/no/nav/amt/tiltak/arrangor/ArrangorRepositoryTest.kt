package no.nav.amt.tiltak.arrangor

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

internal class ArrangorRepositoryTest {

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: ArrangorRepository
	lateinit var testDataRepository: TestDataRepository

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = ArrangorRepository(NamedParameterJdbcTemplate(dataSource))
		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanDatabase(dataSource)
	}

	@Test
	internal fun `upsert() should insert arrangor and return the object`() {
		val organisasjonsnavn = "Test Organisasjon"
		val organisasjonsnummer = "483726374"
		val virksomhetsnavn = "Test Virksomhet"
		val virksomhetsnummer = "123456798"

		val savedDto = repository.upsert(
			overordnetEnhetNavn = organisasjonsnavn,
			overordnetEnhetOrganisasjonsnummer = organisasjonsnummer,
			navn = virksomhetsnavn,
			organisasjonsnummer = virksomhetsnummer
		)

		assertNotNull(savedDto)
		assertNotNull(savedDto.id)
		assertEquals(organisasjonsnummer, savedDto.overordnetEnhetOrganisasjonsnummer)
		assertEquals(organisasjonsnavn, savedDto.overordnetEnhetNavn)
		assertEquals(virksomhetsnavn, savedDto.navn)
		assertEquals(virksomhetsnummer, savedDto.organisasjonsnummer)
	}

	@Test
	internal fun `upsert() same virksomhet twice will return same object`() {
		val overordnetEnhetNavn = "Test Organisasjon"
		val overordnetEnhetOrganisasjonsnummer = "483726374"
		val navn = "Test Virksomhet"
		val organisasjonsnummer = "123456798"

		val savedOne = repository.upsert(
			overordnetEnhetNavn = overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			navn = navn,
			organisasjonsnummer = organisasjonsnummer
		)
		val savedTwo = repository.upsert(
			overordnetEnhetNavn = overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			navn = navn,
			organisasjonsnummer = organisasjonsnummer
		)

		assertEquals(savedOne.id, savedTwo.id)
	}

	@Test
	internal fun `insert() should insert arrangor`() {
		val organisasjonsnavn = "Test Organisasjon"
		val organisasjonsnummer = "483726374"
		val virksomhetsnavn = "Test Virksomhet"
		val virksomhetsnummer = "123456798"
		val id = UUID.randomUUID()

		repository.insert(
			id = id,
			overordnetEnhetNavn = organisasjonsnavn,
			overordnetEnhetOrganisasjonsnummer = organisasjonsnummer,
			navn = virksomhetsnavn,
			organisasjonsnummer = virksomhetsnummer
		)
		val nyArrangor = repository.getById(id)

		assertNotNull(nyArrangor)
		assertNotNull(nyArrangor.id)
		assertEquals(organisasjonsnummer, nyArrangor.overordnetEnhetOrganisasjonsnummer)
		assertEquals(organisasjonsnavn, nyArrangor.overordnetEnhetNavn)
		assertEquals(virksomhetsnavn, nyArrangor.navn)
		assertEquals(virksomhetsnummer, nyArrangor.organisasjonsnummer)
	}

	@Test
	internal fun `getByIder() should return arrangorer`() {
		testDataRepository.insertArrangor(ARRANGOR_1)
		testDataRepository.insertArrangor(ARRANGOR_2)

		val arrangorer = repository.getByIder(listOf(ARRANGOR_1.id, ARRANGOR_2.id))

		arrangorer shouldHaveSize 2
	}

	@Test
	internal fun `getByIder() should not fail with empty list`() {
		repository.getByIder(emptyList()) shouldHaveSize 0
	}

	@Test
	fun `update() - nytt navn og ny overordnet enhet - skal oppdatere arrangor`() {
		testDataRepository.insertArrangor(ARRANGOR_1)
		val updateDbo = ArrangorUpdateDbo(
			id = ARRANGOR_1.id,
			navn = "Nytt navn",
			overordnetEnhetOrganisasjonsnummer = "777888555",
			overordnetEnhetNavn = "Ny overordnet enhet",
		)
		repository.update(updateDbo)

		val oppdatertArrangor = repository.getById(ARRANGOR_1.id)

		oppdatertArrangor.navn shouldBe updateDbo.navn
		oppdatertArrangor.overordnetEnhetOrganisasjonsnummer shouldBe updateDbo.overordnetEnhetOrganisasjonsnummer
		oppdatertArrangor.overordnetEnhetNavn shouldBe updateDbo.overordnetEnhetNavn
	}

	@Test
	fun `update() - overordnet enhet er null - skal oppdatere arrangor`() {
		testDataRepository.insertArrangor(ARRANGOR_1)
		val updateDbo = ArrangorUpdateDbo(
			id = ARRANGOR_1.id,
			navn = ARRANGOR_1.navn,
			overordnetEnhetOrganisasjonsnummer = null,
			overordnetEnhetNavn = null,
		)
		repository.update(updateDbo)

		val oppdatertArrangor = repository.getById(ARRANGOR_1.id)

		oppdatertArrangor.navn shouldBe updateDbo.navn
		oppdatertArrangor.overordnetEnhetOrganisasjonsnummer shouldBe updateDbo.overordnetEnhetOrganisasjonsnummer
		oppdatertArrangor.overordnetEnhetNavn shouldBe updateDbo.overordnetEnhetNavn
	}

	@Test
	fun `updateOverordnetEnhetNavn() - nytt overordnet enhet navn - skal oppdatere navn hos alle underenheter`() {
		val id = UUID.randomUUID()
		val nyOverordnetEnhetNavn = "Ny overordnet enhet"

		testDataRepository.insertArrangor(ARRANGOR_1)
		testDataRepository.insertArrangor(ARRANGOR_1.copy(id = id, organisasjonsnummer = "1234"))

		repository.updateUnderenheterIfAny(ARRANGOR_1.overordnetEnhetOrganisasjonsnummer!!, nyOverordnetEnhetNavn)

		repository.getById(ARRANGOR_1.id).overordnetEnhetNavn shouldBe nyOverordnetEnhetNavn
		repository.getById(id).overordnetEnhetNavn shouldBe nyOverordnetEnhetNavn
	}

}
