package no.nav.amt.tiltak.tiltak.repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Testcontainers
internal class TiltaksinstansRepositoryTest {
	@Container
	val postgresContainer: PostgreSQLContainer<Nothing> =
		PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

	lateinit var jdbcTemplate: JdbcTemplate
	lateinit var template: NamedParameterJdbcTemplate

	lateinit var repository: TiltaksinstansRepository

	companion object TestData {
		val TILTAK_ID = UUID.fromString("9665b0b6-ea7d-44b0-b9c2-8867c2a6c106")
		val TILTAK_INTERNAL_ID = 1

		val TILTAKSLEVERANDOR_ID = UUID.fromString("0dc9ccec-fd1e-4c4e-b91a-c23e6d89c18e")
		val TILTAKSLEVERANDOR_INTERNAL_ID = 1
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

		template = NamedParameterJdbcTemplate(dataSource)
		repository = TiltaksinstansRepository(template)

		jdbcTemplate.update(this::class.java.getResource("/tiltaksinstans-repository_test-data.sql").readText())
	}

	@Test
	internal fun `insert() should insert tiltaksinstans and return object`() {
		val arenaId = 1
		val navn = "TEST Tiltaksinstans"
		val status = null
		val oppstartDato = LocalDate.now().plusDays(2)
		val sluttDato = LocalDate.now().plusDays(10)
		val registrertDato = LocalDateTime.now()
		val fremmoteDato = LocalDateTime.now().plusDays(2).minusHours(2)

		val savedInstans = repository.insert(
			arenaId = arenaId,
			tiltakId = TILTAK_ID,
			tiltaksleverandorId = TILTAKSLEVERANDOR_ID,
			navn = navn,
			status = status,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		)

		assertNotNull(savedInstans)
		assertNotNull(savedInstans.internalId)
		assertNotNull(savedInstans.externalId)

		assertEquals(TILTAK_INTERNAL_ID, savedInstans.tiltakInternalId)
		assertEquals(TILTAK_ID, savedInstans.tiltakExternalId)
		assertEquals(TILTAKSLEVERANDOR_INTERNAL_ID, savedInstans.tiltaksleverandorInternalId)
		assertEquals(TILTAKSLEVERANDOR_ID, savedInstans.tiltaksleverandorExternalId)
		assertEquals(navn, savedInstans.navn)
		assertEquals(status, savedInstans.status)

		assertTrue(oppstartDato!!.isEqualTo(savedInstans.oppstartDato!!))
		assertTrue(sluttDato!!.isEqualTo(savedInstans.sluttDato!!))
		assertTrue(registrertDato!!.isEqualTo(savedInstans.registrertDato!!))
		assertTrue(fremmoteDato!!.isEqualTo(savedInstans.fremmoteDato!!))
	}

	@Test
	internal fun `getByArenaId returns the correct object`() {
		val arenaId = 1
		val navn = "TEST Tiltaksinstans"
		val status = null
		val oppstartDato = LocalDate.now().plusDays(2)
		val sluttDato = LocalDate.now().plusDays(10)
		val registrertDato = LocalDateTime.now()
		val fremmoteDato = LocalDateTime.now().plusDays(2).minusHours(2)

		val savedInstans = repository.insert(
			arenaId = arenaId,
			tiltakId = TILTAK_ID,
			tiltaksleverandorId = TILTAKSLEVERANDOR_ID,
			navn = navn,
			status = status,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		)

		val gottenObject = repository.getByArenaId(arenaId)

		assertEquals(savedInstans, gottenObject)
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


/**
 * A helping function as SQL Timestamp and LocalDateTime does not have the same precision
 */
fun LocalDateTime.isEqualTo(other: LocalDateTime): Boolean {
	return this.year == other.year
		&& this.month == other.month
		&& this.dayOfMonth == other.dayOfMonth
		&& this.hour == other.hour
		&& this.minute == other.minute
		&& this.second == other.second

}

fun LocalDate.isEqualTo(other: LocalDate): Boolean {
	return this.year == other.year
		&& this.month == other.month
		&& this.dayOfMonth == other.dayOfMonth
}
