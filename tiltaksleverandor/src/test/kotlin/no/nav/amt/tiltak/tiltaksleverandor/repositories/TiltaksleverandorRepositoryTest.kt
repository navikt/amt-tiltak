package no.nav.amt.tiltak.tiltaksleverandor.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer

internal class TiltaksleverandorRepositoryTest {

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: TiltaksleverandorRepository

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = TiltaksleverandorRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanDatabase(dataSource)
	}

	@Test
	internal fun `insert() should insert tiltaksleverandor and return the object`() {
		val organisasjonsnavn = "Test Organisasjon"
		val organisasjonsnummer = "483726374"
		val virksomhetsnavn = "Test Virksomhet"
		val virksomhetsnummer = "123456798"

		val savedDto = repository.insert(
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
	internal fun `insert() same virksomhet twice will return same object`() {
		val overordnetEnhetNavn = "Test Organisasjon"
		val overordnetEnhetOrganisasjonsnummer = "483726374"
		val navn = "Test Virksomhet"
		val organisasjonsnummer = "123456798"

		val savedOne = repository.insert(
			overordnetEnhetNavn = overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			navn = navn,
			organisasjonsnummer = organisasjonsnummer
		)
		val savedTwo = repository.insert(
			overordnetEnhetNavn = overordnetEnhetNavn,
			overordnetEnhetOrganisasjonsnummer = overordnetEnhetOrganisasjonsnummer,
			navn = navn,
			organisasjonsnummer = organisasjonsnummer
		)

		assertEquals(savedOne.id, savedTwo.id)
	}

}
