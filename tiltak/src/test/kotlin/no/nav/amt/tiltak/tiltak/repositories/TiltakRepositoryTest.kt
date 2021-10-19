package no.nav.amt.tiltak.tiltak.repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*

@Testcontainers
internal class TiltakRepositoryTest {

	@Container
	val postgresContainer: PostgreSQLContainer<Nothing> =
		PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

	lateinit var jdbcTemplate: JdbcTemplate

	lateinit var repository: TiltakRepository

	companion object TestData {
		val TILTAKSLEVERANDOR_1_ID = UUID.fromString("0dc9ccec-fd1e-4c4e-b91a-c23e6d89c18e")
		val TILTAKSLEVERANDOR_2_ID = UUID.fromString("4d1ad2c5-71a6-472b-a3bb-79aaa262527b")
		val TILTAKSLEVERANDOR_ID_NOT_EXIST = UUID.fromString("3cc09a7b-147b-4b0f-b186-e24cb199c8dc")
	}

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
		repository = TiltakRepository(jdbcTemplate)

		jdbcTemplate.update(this::class.java.getResource("/test-data.sql").readText())
	}

	@Test
	internal fun `insert() should insert tiltak and return object`() {
		val tiltak = Tiltak(
			id = null,
			tiltaksleverandorId = TILTAKSLEVERANDOR_1_ID,
			kode = "AMO",
			navn = "Dette er et testtiltak"
		)

		val savedDto = repository.insert(tiltak)

		assertNotNull(savedDto)
		assertNotNull(savedDto.id)
		assertNotNull(savedDto.externalId)

		assertEquals(tiltak.kode, savedDto.type)
		assertEquals(tiltak.navn, savedDto.navn)

	}

	//TODO Hvorfor feiler ikke denne?
	@Test
	internal fun `insert on nonexistent tiltaksleverandor should throw`() {
		val tiltak = Tiltak(
			id = null,
			tiltaksleverandorId = TILTAKSLEVERANDOR_ID_NOT_EXIST,
			kode = "AMO",
			navn = "VIL FEILE?!"
		)

		assertThrows<Exception> { repository.insert(tiltak) }
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
