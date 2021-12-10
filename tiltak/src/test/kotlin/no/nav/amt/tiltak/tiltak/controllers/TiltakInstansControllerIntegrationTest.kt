package no.nav.amt.tiltak.tiltak.controllers

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.core.port.TiltakInstansService
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltak.dbo.TiltakInstansDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.NavAnsattRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakInstansRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.services.DeltakerServiceImpl
import no.nav.amt.tiltak.tiltak.services.TiltakInstansServiceImpl
import no.nav.amt.tiltak.tiltak.services.TiltakServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TiltakInstansControllerIntegrationTest {

	private val dataSource = SingletonPostgresContainer.getDataSource()

	private lateinit var namedJdbcTemplate: NamedParameterJdbcTemplate
	private lateinit var tiltakRepository: TiltakRepository
	private lateinit var deltakerRepository: DeltakerRepository
	private lateinit var brukerRepository: BrukerRepository
	private lateinit var tiltakInstansRepository: TiltakInstansRepository
	private lateinit var tiltakInstansService: TiltakInstansService
	private lateinit var deltakerService: DeltakerService
	private lateinit var controller: TiltakInstansController
	private var tiltakKode = "GRUPPEAMO"
	private var epost = "bla@bla.com"

	@BeforeEach
	fun before() {
		namedJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

		tiltakInstansRepository = TiltakInstansRepository(namedJdbcTemplate)
		tiltakRepository = TiltakRepository(namedJdbcTemplate)
		deltakerRepository = DeltakerRepository(namedJdbcTemplate)
		brukerRepository = BrukerRepository(namedJdbcTemplate)
		deltakerService = DeltakerServiceImpl(
			deltakerRepository,
			brukerRepository,
			mock(NavAnsattRepository::class.java),
			mock(PersonService::class.java)
		);
		tiltakInstansService = TiltakInstansServiceImpl(tiltakInstansRepository, TiltakServiceImpl(tiltakRepository))
		controller = TiltakInstansController(tiltakInstansService, deltakerService)

		DatabaseTestUtils.cleanDatabase(dataSource)

	}

	@Test
	fun `hentTiltakInstans - tiltaksgjennomføring finnes ikke - skal returnere NOT FOUND`() {
		val id = UUID.randomUUID()
		val exception = assertThrows(ResponseStatusException::class.java) {
			controller.hentTiltakInstans(id)
		}
		assertEquals("404 NOT_FOUND", exception.status.toString())
	}

	@Test
	fun `hentTiltakInstans - tiltak finnes ikke - skal returnere INTERNAL SERVER ERROR`() {
		val tiltakNavn = "Gruppe amo"
		val tlId = insertArrangor()
		val tiltak = tiltakRepository.insert("22", tiltakNavn, "kode")
		val instans = insertTiltakInstans(tiltak.id, tlId)

		val resultat = controller.hentTiltakInstans(instans.id)

		assertEquals(instans.id, resultat.id)
		assertEquals(tiltakNavn, resultat.tiltak.tiltaksnavn)
	}

	@Test
	fun `hentTiltakInstans - tiltak finnes - skal returnere tiltak`() {
		val arrangorId = insertArrangor()
		val tiltak = tiltakRepository.insert("4", tiltakKode, tiltakKode)
		val tiltakInstans = insertTiltakInstans(tiltak.id, arrangorId)
		val resultat = controller.hentTiltakInstans(tiltakInstans.id)

		assertEquals(tiltakInstans.id, resultat.id)
		assertEquals(tiltakInstans.navn, resultat.navn)

	}

	@Test
	fun `hentDeltakere - En deltaker på tiltak`() {
		val arrangorId = insertArrangor()
		val tiltak = tiltakRepository.insert((1000..9999).random().toString(), tiltakKode, tiltakKode)

		val tiltakInstans = insertTiltakInstans(tiltak.id, arrangorId)
		val bruker1 = BrukerInsertDbo("12128673847", "Person", "En", "To", "123", epost, null)
		val startDato = LocalDate.now().minusDays(5)
		val sluttDato = LocalDate.now().plusDays(3)
		val regDato = LocalDateTime.now().minusDays(10)
		insertDeltaker(tiltakInstans.id, bruker1, startDato, sluttDato, regDato)

		val deltakere = controller.hentDeltakere(tiltakInstans.id)
		val deltaker1 = deltakere.get(0)

		deltakere.size shouldBe 1
		deltaker1.fodselsnummer shouldBe bruker1.fodselsnummer
		deltaker1.fornavn shouldBe bruker1.fornavn
		deltaker1.etternavn shouldBe bruker1.etternavn

		deltaker1.oppstartdato shouldBe startDato
		deltaker1.sluttdato shouldBe sluttDato
		deltaker1.registrertDato.truncatedTo(ChronoUnit.MINUTES) shouldBe regDato.truncatedTo(ChronoUnit.MINUTES)

	}

	@Test
	fun `hentDeltakere - Flere deltakere finnes`() {
		val arrangorId = insertArrangor()
		val tiltak = tiltakRepository.insert((1000..9999).random().toString(), tiltakKode, tiltakKode)

		val tiltakInstans = insertTiltakInstans(tiltak.id, arrangorId)
		val bruker1 = BrukerInsertDbo("12128673847", "Person", "En", "To", "123", epost, null)
		val bruker2 = BrukerInsertDbo("12128674909", "Person", "En", "To", "123", epost, null)

		insertDeltaker(tiltakInstans.id, bruker1, LocalDate.now().minusDays(3), LocalDate.now().plusDays(1), LocalDateTime.now().minusDays(10))
		insertDeltaker(tiltakInstans.id, bruker2, LocalDate.now().minusDays(3), LocalDate.now().plusDays(1), LocalDateTime.now().minusDays(10))

		val deltakere = controller.hentDeltakere(tiltakInstans.id)
		val deltaker1 = deltakere.get(0)
		val deltaker2 = deltakere.get(1)

		deltakere.size shouldBe 2
		deltaker1.fodselsnummer shouldBe bruker1.fodselsnummer
		deltaker2.fodselsnummer shouldBe bruker2.fodselsnummer
	}

	private fun insertDeltaker(instansId: UUID, bruker: BrukerInsertDbo, startDato: LocalDate, sluttDato: LocalDate, regDato: LocalDateTime): DeltakerDbo {
		val bruker = brukerRepository.insert(bruker)
		return deltakerRepository.insert(
			brukerId = bruker.id,
			tiltaksgjennomforingId = instansId,
			oppstartDato = startDato,
			sluttDato = sluttDato,
			status = Deltaker.Status.GJENNOMFORES,
			dagerPerUke = 5,
			prosentStilling = 10f,
			registrertDato = regDato
		)
	}

	private fun insertTiltakInstans(tiltakId: UUID, arrangorId: UUID): TiltakInstansDbo {
		var arenaId = (1000..9999).random()
		return tiltakInstansRepository.insert(
			arenaId = arenaId,
			tiltakId = tiltakId,
			arrangorId = arrangorId,
			navn = "Kaffekurs",
			status = TiltakInstans.Status.GJENNOMFORES,
			oppstartDato = null,
			sluttDato = null,
			registrertDato = null,
			fremmoteDato = null
		)
	}

	private fun insertArrangor(): UUID {
		val id = UUID.randomUUID()
		val orgnr = (1000..9999).random()
		val insert = """
			INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer,navn)
			VALUES ('$id', '12345678', 'Orgnavn1', '$orgnr', 'Virksomhetsnavn1')
		""".trimIndent()
		namedJdbcTemplate.jdbcTemplate.update(insert)

		return id
	}

}
