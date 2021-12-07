package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavKontorService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.core.port.TiltakInstansService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.NavAnsattRepository
import no.nav.amt.tiltak.deltaker.repositories.NavKontorRepository
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltak.dbo.TiltakInstansDbo
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
			mock(NavKontorRepository::class.java),
			mock(NavKontorService::class.java),
			mock(PersonService::class.java),
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
		val tiltak = tiltakRepository.insert("4", "Gruppe AMO", "GRUPPEAMO")
		val tiltakInstans = insertTiltakInstans(tiltak.id, arrangorId)
		val resultat = controller.hentTiltakInstans(tiltakInstans.id)
		assertEquals(tiltakInstans.id, resultat.id)
		assertEquals(tiltakInstans.navn, resultat.navn)

	}

	@Test
	fun `hentDeltakere - happy path`() {
		val arrangorId = insertArrangor()
		val tiltak = tiltakRepository.insert((1000..9999).random().toString(), "Gruppe AMO", "GRUPPEAMO")

		val tiltakInstans = insertTiltakInstans(tiltak.id, arrangorId)

		insertDeltaker(tiltakInstans.id, "12128673847")
		insertDeltaker(tiltakInstans.id, "12128673846")

		val deltakere = controller.hentDeltakere(tiltakInstans.id)

		assertEquals(deltakere.size, 2)
	}

	private fun insertDeltaker(instansId: UUID, fnr: String): DeltakerDbo {
		val bruker = brukerRepository.insert(
			fodselsnummer = fnr,
			fornavn = "Fornavn",
			mellomnavn = "",
			etternavn = "Etternavn",
			telefonnummer = "12345678",
			epost = "epost",
			ansvarligVeilederId = null,
			navKontorId = null
		)
		return deltakerRepository.insert(
			brukerId = bruker.id,
			tiltaksgjennomforingId = instansId,
			oppstartDato = LocalDate.now(),
			sluttDato = LocalDate.now(),
			status = Deltaker.Status.GJENNOMFORES,
			arenaStatus = "arenastatus",
			dagerPerUke = 5,
			prosentStilling = 10f
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
