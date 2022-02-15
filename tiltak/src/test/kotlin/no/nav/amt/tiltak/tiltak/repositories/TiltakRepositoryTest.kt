package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

internal class TiltakRepositoryTest {

	private val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: TiltakRepository

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = TiltakRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	internal fun `insert() should insert tiltak and return object`() {
		val id = UUID.randomUUID()
		val navn = "Navn"
		val kode = "Kode"

		val savedDbo = repository.insert(id, navn, kode)

		assertNotNull(savedDbo)
		assertNotNull(savedDbo.id)

		assertEquals(id, savedDbo.id)
		assertEquals(kode, savedDbo.type)
		assertEquals(navn, savedDbo.navn)

	}

	@Test
	internal fun `update() should throw if tiltak does not exist`() {
		assertThrows<NoSuchElementException> {
			repository.update(
				id = UUID.randomUUID(),
				navn = "aoisdfj",
				type = "jdasiofjasf",
			)
		}
	}

	@Test
	internal fun `update() should return updatedObject`() {
		val updatedNavn = "UpdatedNavn"
		val updatedKode = "UpdatedKode"

		val updatedTiltak = repository.update(
			id  = TILTAK_1.id,
			navn = updatedNavn,
			type = updatedKode
		)

		assertEquals(updatedNavn, updatedTiltak.navn)
		assertEquals(updatedKode, updatedTiltak.type)
	}

}
