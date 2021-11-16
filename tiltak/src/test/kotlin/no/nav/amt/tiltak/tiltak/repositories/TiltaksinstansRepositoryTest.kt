package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.dbo.TiltaksinstansDbo
import no.nav.amt.tiltak.tiltak.testutils.DatabaseTestUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Testcontainers
internal class TiltaksinstansRepositoryTest {
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
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		template = DatabaseTestUtils.getDatabase("/tiltaksinstans-repository_test-data.sql")
		repository = TiltaksinstansRepository(template)
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
	internal fun `update() should throw if tiltaksinstans does not exist`() {
		assertThrows<NoSuchElementException> {
			repository.update(
				TiltaksinstansDbo(
					internalId = 999,
					externalId = UUID.randomUUID(),
					arenaId = 9999,
					tiltaksleverandorInternalId = 9999,
					tiltaksleverandorExternalId = UUID.randomUUID(),
					tiltakInternalId = 9999,
					tiltakExternalId = UUID.randomUUID(),
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
		val updatedNavn = "UpdatedNavn"
		val updatedStatus = TiltakInstans.Status.GJENNOMFORES
		val updatedOppstartsdato = LocalDate.now().plusDays(4)
		val updatedSluttdato = LocalDate.now().plusDays(14)
		val updatedFremmotedato = LocalDateTime.now().plusDays(4)

		val newTiltakInstans = repository.insert(
			arenaId = 1,
			tiltakId = TILTAK_ID,
			tiltaksleverandorId = TILTAKSLEVERANDOR_ID,
			"Navn",
			status = null,
			oppstartDato = null,
			sluttDato = null,
			registrertDato = LocalDateTime.now(),
			fremmoteDato = null
		)

		val updatedTiltaksinstans = repository.update(
			newTiltakInstans.copy(
				navn = updatedNavn,
				status = updatedStatus,
				oppstartDato = updatedOppstartsdato,
				sluttDato = updatedSluttdato,
				fremmoteDato = updatedFremmotedato
			)
		)

		assertEquals(updatedNavn, updatedTiltaksinstans.navn)
		assertEquals(updatedStatus, updatedTiltaksinstans.status)
		assertTrue(updatedOppstartsdato.isEqualTo(updatedTiltaksinstans.oppstartDato))
		assertTrue(updatedSluttdato.isEqualTo(updatedTiltaksinstans.sluttDato))
		assertTrue(updatedFremmotedato.isEqualTo(updatedTiltaksinstans.fremmoteDato))
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
