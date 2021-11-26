package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltak.dbo.TiltakDbo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime
import java.util.*

internal class TiltakRepositoryTest {

	private val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: TiltakRepository

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = TiltakRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanDatabase(dataSource)
		DatabaseTestUtils.runScriptFile("/tiltak-repository_test-data.sql", dataSource)
	}

	@Test
	internal fun `insert() should insert tiltak and return object`() {
		val arenaId = "1"
		val navn = "Navn"
		val kode = "Kode"

		val savedDbo = repository.insert(arenaId, navn, kode)

		assertNotNull(savedDbo)
		assertNotNull(savedDbo.id)

		assertEquals(arenaId, savedDbo.arenaId)
		assertEquals(kode, savedDbo.type)
		assertEquals(navn, savedDbo.navn)

	}

	@Test
	internal fun `update() should throw if tiltak does not exist`() {
		assertThrows<NoSuchElementException> {
			repository.update(
				TiltakDbo(
					id = UUID.randomUUID(),
					arenaId = "sdiofj",
					navn = "aoisdfj",
					type = "jdasiofjasf",
					createdAt = LocalDateTime.now(),
					modifiedAt = LocalDateTime.now()
				)
			)
		}
	}

	@Test
	internal fun `update() should return updatedObject`() {
		val arenaId = "1"
		val navn = "Navn"
		val kode = "Kode"

		val updatedNavn = "UpdatedNavn"
		val updatedKode = "UpdatedKode"

		val newTiltak = repository.insert(arenaId, navn, kode)
		val updatedTiltak = repository.update(
			newTiltak.copy(
				navn = updatedNavn,
				type = updatedKode
			)
		)

		assertEquals(updatedNavn, updatedTiltak.navn)
		assertEquals(updatedKode, updatedTiltak.type)
	}

	@Test
	internal fun `getByArenaId returns the correct object`() {
		val arenaId = "1"
		val navn = "Navn"
		val kode = "Kode"

		repository.insert(arenaId, navn, kode)

		val savedDbo = repository.getByArenaId(arenaId)

		assertNotNull(savedDbo)
		assertEquals(kode, savedDbo?.type)
		assertEquals(navn, savedDbo?.navn)

	}

}
