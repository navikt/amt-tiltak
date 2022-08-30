package no.nav.amt.tiltak.arrangor

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.UUID

internal class ArrangorRepositoryTest {

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: ArrangorRepository

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = ArrangorRepository(NamedParameterJdbcTemplate(dataSource))

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

}
