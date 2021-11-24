package no.nav.amt.tiltak.tiltaksleverandor.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
internal class TiltaksleverandorRepositoryTest {

	@Container
	val postgresContainer: PostgreSQLContainer<Nothing> =
		PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

	lateinit var jdbcTemplate: JdbcTemplate
	lateinit var template: NamedParameterJdbcTemplate

	lateinit var repository: TiltaksleverandorRepository

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		val dataSource = createDataSource(postgresContainer)

		val flyway: Flyway = Flyway.configure()
			.dataSource(dataSource)
			.load()

		flyway.clean()
		flyway.migrate()

		jdbcTemplate = JdbcTemplate(dataSource)
		template = NamedParameterJdbcTemplate(dataSource)
		repository = TiltaksleverandorRepository(template)
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

	private fun createDataSource(container: PostgreSQLContainer<Nothing>): HikariDataSource {
		val config = HikariConfig()
		config.username = container.username
		config.password = container.password
		config.jdbcUrl = container.jdbcUrl
		config.driverClassName = container.driverClassName
		return HikariDataSource(config)
	}
}
