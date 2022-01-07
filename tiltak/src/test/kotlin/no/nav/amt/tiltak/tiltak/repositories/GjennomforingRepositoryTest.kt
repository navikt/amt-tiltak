package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

internal class GjennomforingRepositoryTest {

	private val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: GjennomforingRepository

	companion object TestData {
		val TILTAK_ID = UUID.fromString("9665b0b6-ea7d-44b0-b9c2-8867c2a6c106")
		val ARRANGOR_ID = UUID.fromString("0dc9ccec-fd1e-4c4e-b91a-c23e6d89c18e")
	}

	@BeforeEach
	fun migrate() {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = GjennomforingRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/gjennomforing-repository_test-data.sql")
	}

	@Test
	internal fun `insert() should insert gjennomforing and return object`() {
		val id = UUID.randomUUID()
		val navn = "TEST Tiltaksgjennomforing"
		val status = null
		val oppstartDato = LocalDate.now().plusDays(2)
		val sluttDato = LocalDate.now().plusDays(10)
		val registrertDato = LocalDateTime.now()
		val fremmoteDato = LocalDateTime.now().plusDays(2).minusHours(2)

		val savedGjennomforing = repository.insert(
			id = id,
			tiltakId = TILTAK_ID,
			arrangorId = ARRANGOR_ID,
			navn = navn,
			status = status,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		)

		assertNotNull(savedGjennomforing)
		assertNotNull(savedGjennomforing.id)

		assertEquals(TILTAK_ID, savedGjennomforing.tiltakId)
		assertEquals(ARRANGOR_ID, savedGjennomforing.arrangorId)
		assertEquals(navn, savedGjennomforing.navn)
		assertEquals(status, savedGjennomforing.status)

		assertTrue(oppstartDato!!.isEqualTo(savedGjennomforing.oppstartDato!!))
		assertTrue(sluttDato!!.isEqualTo(savedGjennomforing.sluttDato!!))
		assertTrue(registrertDato!!.isEqualTo(savedGjennomforing.registrertDato!!))
		assertTrue(fremmoteDato!!.isEqualTo(savedGjennomforing.fremmoteDato!!))
	}

	@Test
	internal fun `update() should throw if gjennomforing does not exist`() {
		assertThrows<NoSuchElementException> {
			repository.update(
				GjennomforingDbo(
					id = UUID.randomUUID(),
					arrangorId = UUID.randomUUID(),
					tiltakId = UUID.randomUUID(),
					navn = "idosfja",
					status = null,
					oppstartDato = null,
					sluttDato = null,
					registrertDato = LocalDateTime.now(),
					fremmoteDato = null,
					createdAt = LocalDateTime.now(),
					modifiedAt = LocalDateTime.now()
				)
			)
		}
	}

	@Test
	internal fun `update() should return updated object`() {
		val id = UUID.randomUUID()
		val updatedNavn = "UpdatedNavn"
		val updatedStatus = Gjennomforing.Status.GJENNOMFORES
		val updatedOppstartsdato = LocalDate.now().plusDays(4)
		val updatedSluttdato = LocalDate.now().plusDays(14)
		val updatedFremmotedato = LocalDateTime.now().plusDays(4)

		val newGjennomforing = repository.insert(
			id = id,
			tiltakId = TILTAK_ID,
			arrangorId = ARRANGOR_ID,
			"Navn",
			status = null,
			oppstartDato = null,
			sluttDato = null,
			registrertDato = LocalDateTime.now(),
			fremmoteDato = null
		)

		val updatedGjennomforing = repository.update(
			newGjennomforing.copy(
				navn = updatedNavn,
				status = updatedStatus,
				oppstartDato = updatedOppstartsdato,
				sluttDato = updatedSluttdato,
				fremmoteDato = updatedFremmotedato
			)
		)

		assertEquals(updatedNavn, updatedGjennomforing.navn)
		assertEquals(updatedStatus, updatedGjennomforing.status)
		assertTrue(updatedOppstartsdato.isEqualTo(updatedGjennomforing.oppstartDato))
		assertTrue(updatedSluttdato.isEqualTo(updatedGjennomforing.sluttDato))
		assertTrue(updatedFremmotedato.isEqualTo(updatedGjennomforing.fremmoteDato))
	}

}


/**
 * A helping function as SQL Timestamp and LocalDateTime does not have the same precision
 */
fun LocalDateTime.isEqualTo(other: LocalDateTime?): Boolean {
	if (other == null) {
		return false
	}

	return this.year == other.year
		&& this.month == other.month
		&& this.dayOfMonth == other.dayOfMonth
		&& this.hour == other.hour
		&& this.minute == other.minute
		&& this.second == other.second

}

fun LocalDate.isEqualTo(other: LocalDate?): Boolean {
	if (other == null) {
		return false
	}

	return this.year == other.year
		&& this.month == other.month
		&& this.dayOfMonth == other.dayOfMonth
}
