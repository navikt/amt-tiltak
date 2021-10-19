package no.nav.amt.tiltak.tiltaksleverandor.repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Virksomhet
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
internal class TiltaksleverandorServiceRepositoryTest {

	@Container
	val postgresContainer: PostgreSQLContainer<Nothing> =
		PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

	lateinit var jdbcTemplate: JdbcTemplate

	lateinit var repository: TiltaksleverandorRepository

	@BeforeEach
	fun migrate() {
		val dataSource = createDataSource(postgresContainer)

		// TODO: Kopiert fra LocalPostgresDatabase.kt. Hadde det vært bedre med en modul for test-verktøy?
		val flyway: Flyway = Flyway.configure()
			.dataSource(dataSource)
			.load()

		flyway.clean()
		flyway.migrate()

		jdbcTemplate = JdbcTemplate(dataSource)
		repository = TiltaksleverandorRepository(jdbcTemplate)

//		jdbcTemplate.update(this::class.java.getResource("/arena-data.sql").readText())
	}

	@Test
	internal fun `insert() should insert tiltaksleverandor and return the object`() {
		val virksomhet = Virksomhet(
			organisasjonsnavn = "Test Organisasjon",
			organisasjonsnummer = "483726374",
			virksomhetsnavn = "Test Virksomhet",
			virksomhetsnummer = "123456798"
		)

		val savedDto = repository.insert(virksomhet)

		assertNotNull(savedDto)
		assertNotNull(savedDto.id)
		assertNotNull(savedDto.externalId)
		assertEquals(virksomhet.organisasjonsnummer, savedDto.organisasjonsnummer)
		assertEquals(virksomhet.organisasjonsnavn, savedDto.organisasjonsnavn)
		assertEquals(virksomhet.virksomhetsnavn, savedDto.virksomhetsnavn)
		assertEquals(virksomhet.virksomhetsnummer, savedDto.virksomhetsnummer)
	}

	@Test
	internal fun `insert() same virksomhet twice will return same object`() {
		val virksomhet = Virksomhet(
			organisasjonsnavn = "Test Organisasjon",
			organisasjonsnummer = "483726374",
			virksomhetsnavn = "Test Virksomhet",
			virksomhetsnummer = "123456798"
		)

		val savedOne = repository.insert(virksomhet)
		val savedTwo = repository.insert(virksomhet)

		assertEquals(savedOne.id, savedTwo.id)
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
