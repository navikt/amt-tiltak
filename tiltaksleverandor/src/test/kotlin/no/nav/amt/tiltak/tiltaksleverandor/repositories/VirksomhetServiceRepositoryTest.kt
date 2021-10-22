package no.nav.amt.tiltak.tiltaksleverandor.repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
internal class VirksomhetServiceRepositoryTest {

	@Container
	val postgresContainer: PostgreSQLContainer<Nothing> =
		PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

	lateinit var jdbcTemplate: JdbcTemplate
	lateinit var template: NamedParameterJdbcTemplate

	lateinit var repository: TiltaksleverandorRepository

	@BeforeEach
	fun migrate() {
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
			organisasjonsnavn = organisasjonsnavn,
			organisasjonsnummer = organisasjonsnummer,
			virksomhetsnavn = virksomhetsnavn,
			virksomhetsnummer = virksomhetsnummer
		)

		assertNotNull(savedDto)
		assertNotNull(savedDto.internalId)
		assertNotNull(savedDto.externalId)
		assertEquals(organisasjonsnummer, savedDto.organisasjonsnummer)
		assertEquals(organisasjonsnavn, savedDto.organisasjonsnavn)
		assertEquals(virksomhetsnavn, savedDto.virksomhetsnavn)
		assertEquals(virksomhetsnummer, savedDto.virksomhetsnummer)
	}

	@Test
	internal fun `insert() same virksomhet twice will return same object`() {
		val organisasjonsnavn = "Test Organisasjon"
		val organisasjonsnummer = "483726374"
		val virksomhetsnavn = "Test Virksomhet"
		val virksomhetsnummer = "123456798"

		val savedOne = repository.insert(
			organisasjonsnavn = organisasjonsnavn,
			organisasjonsnummer = organisasjonsnummer,
			virksomhetsnavn = virksomhetsnavn,
			virksomhetsnummer = virksomhetsnummer
		)
		val savedTwo = repository.insert(
			organisasjonsnavn = organisasjonsnavn,
			organisasjonsnummer = organisasjonsnummer,
			virksomhetsnavn = virksomhetsnavn,
			virksomhetsnummer = virksomhetsnummer
		)

		assertEquals(savedOne.internalId, savedTwo.internalId)
		assertEquals(savedOne.externalId, savedTwo.externalId)
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
