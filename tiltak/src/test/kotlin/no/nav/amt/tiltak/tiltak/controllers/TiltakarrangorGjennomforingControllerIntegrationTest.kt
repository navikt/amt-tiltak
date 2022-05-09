package no.nav.amt.tiltak.tiltak.controllers

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.amt.tiltak.arrangor.ArrangorRepository
import no.nav.amt.tiltak.arrangor.ArrangorServiceImpl
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.services.BrukerServiceImpl
import no.nav.amt.tiltak.tiltak.services.GjennomforingServiceImpl
import no.nav.amt.tiltak.tiltak.services.TiltakServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TiltakarrangorGjennomforingControllerIntegrationTest {

	private val dataSource = SingletonPostgresContainer.getDataSource()

	private lateinit var namedJdbcTemplate: NamedParameterJdbcTemplate
	private lateinit var tiltakRepository: TiltakRepository
	private lateinit var deltakerRepository: DeltakerRepository
	private lateinit var brukerRepository: BrukerRepository
	private lateinit var brukerService: BrukerServiceImpl
	private lateinit var deltakerStatusRepository: DeltakerStatusRepository
	private lateinit var gjennomforingRepository: GjennomforingRepository
	private lateinit var gjennomforingService: GjennomforingService
	private lateinit var deltakerService: DeltakerService
	private lateinit var arrangorService: ArrangorService
	private lateinit var authService: AuthService
	private lateinit var controller: TiltakarrangorGjennomforingController
	private var tiltakKode = "GRUPPEAMO"
	private var epost = "bla@bla.com"

	@BeforeEach
	fun before() {
		val transactionTemplate = TransactionTemplate(DataSourceTransactionManager(dataSource))

		namedJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

		gjennomforingRepository = GjennomforingRepository(namedJdbcTemplate)
		tiltakRepository = TiltakRepository(namedJdbcTemplate)
		deltakerRepository = DeltakerRepository(namedJdbcTemplate)
		brukerRepository = BrukerRepository(namedJdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(namedJdbcTemplate)
		authService = mock(AuthService::class.java)
		arrangorService = ArrangorServiceImpl(mockk(), ArrangorRepository(namedJdbcTemplate))
		brukerService = BrukerServiceImpl(
			brukerRepository,
			mock(PersonService::class.java),
			mock(NavAnsattService::class.java),
			mock(NavEnhetService::class.java)
		)
		deltakerService = DeltakerServiceImpl(
			deltakerRepository,
			deltakerStatusRepository,
			brukerService,
			transactionTemplate
		)
		gjennomforingService = GjennomforingServiceImpl(
			gjennomforingRepository,
			TiltakServiceImpl(tiltakRepository),
			deltakerService,
			arrangorService,
			transactionTemplate
		)
		controller = TiltakarrangorGjennomforingController(
			gjennomforingService, deltakerService,
			authService, mock(ArrangorAnsattTilgangService::class.java)
		)

		DbTestDataUtils.cleanDatabase(dataSource)
	}

	@Test
	fun `hentGjennomforing - tiltaksgjennomføring finnes ikke - skal returnere NOT FOUND`() {
		val id = UUID.randomUUID()
		val exception = assertThrows(ResponseStatusException::class.java) {
			controller.hentGjennomforing(id)
		}
		assertEquals("404 NOT_FOUND", exception.status.toString())
	}

	@Test
	fun `hentGjennomforinger - tiltak finnes - skal returnere gjennomføring med tiltak`() {
		val tiltakNavn = "Gruppe amo"
		val orgNavn = "orgnavn"
		val gjennomforingNavn = "Gjennomføringnavn"
		val arrangor = insertArrangor(orgNavn)
		val tiltak = tiltakRepository.insert(UUID.randomUUID(), tiltakNavn, "kode")
		val gjennomforing = insertGjennomforing(tiltak.id, arrangor, gjennomforingNavn)

		val resultat = controller.hentGjennomforing(gjennomforing.id)

		assertEquals(gjennomforing.id, resultat.id)
		assertEquals(gjennomforingNavn, resultat.navn)
		assertEquals(tiltakNavn, resultat.tiltak.tiltaksnavn)
		assertEquals(orgNavn, resultat.arrangor.organisasjonNavn)

	}

	@Test
	fun `hentDeltakere - En deltaker på tiltak`() {
		val arrangorId = insertArrangor()
		val tiltak = tiltakRepository.insert(UUID.randomUUID(), tiltakKode, tiltakKode)

		val gjennomforing = insertGjennomforing(tiltak.id, arrangorId)
		val bruker1 = BrukerInsertDbo("12128673847", "Person", "En", "To", "123", epost, null, null)
		val startDato = LocalDate.now().minusDays(5)
		val sluttDato = LocalDate.now().plusDays(3)
		val regDato = LocalDateTime.now().minusDays(10)
		insertDeltaker(gjennomforing.id, bruker1, startDato, sluttDato, regDato)

		val deltakere = controller.hentDeltakere(gjennomforing.id)
		val deltaker1 = deltakere.get(0)

		deltakere.size shouldBe 1
		deltaker1.fodselsnummer shouldBe bruker1.fodselsnummer
		deltaker1.fornavn shouldBe bruker1.fornavn
		deltaker1.etternavn shouldBe bruker1.etternavn

		deltaker1.startDato shouldBe startDato
		deltaker1.sluttDato shouldBe sluttDato
		deltaker1.registrertDato.truncatedTo(ChronoUnit.MINUTES) shouldBe regDato.truncatedTo(ChronoUnit.MINUTES)

	}

	@Test
	fun `hentDeltakere - Flere deltakere finnes`() {
		val arrangorId = insertArrangor()
		val tiltak = tiltakRepository.insert(UUID.randomUUID(), tiltakKode, tiltakKode)

		val gjennomforing = insertGjennomforing(tiltak.id, arrangorId)
		val bruker1 = BrukerInsertDbo("12128673847", "Person", "En", "To", "123", epost, null, null)
		val bruker2 = BrukerInsertDbo("12128674909", "Person", "En", "To", "123", epost, null, null)

		insertDeltaker(
			gjennomforing.id,
			bruker1,
			LocalDate.now().minusDays(3),
			LocalDate.now().plusDays(1),
			LocalDateTime.now().minusDays(10)
		)
		insertDeltaker(
			gjennomforing.id,
			bruker2,
			LocalDate.now().minusDays(3),
			LocalDate.now().plusDays(1),
			LocalDateTime.now().minusDays(10)
		)

		val deltakere = controller.hentDeltakere(gjennomforing.id)
		val deltaker1 = deltakere.get(0)
		val deltaker2 = deltakere.get(1)

		deltakere.size shouldBe 2
		deltaker1.fodselsnummer shouldBe bruker1.fodselsnummer
		deltaker2.fodselsnummer shouldBe bruker2.fodselsnummer
	}

	private fun insertDeltaker(
		gjennomforingId: UUID,
		bruker: BrukerInsertDbo,
		startDato: LocalDate,
		sluttDato: LocalDate,
		regDato: LocalDateTime
	): DeltakerDbo {
		val bruker = brukerRepository.insert(bruker)
		val deltaker = deltakerRepository.insert(
			id = UUID.randomUUID(),
			brukerId = bruker.id,
			gjennomforingId = gjennomforingId,
			startDato = startDato,
			sluttDato = sluttDato,
			dagerPerUke = 5,
			prosentStilling = 10f,
			registrertDato = regDato
		)
		insertStatus(deltaker.id)
		return deltaker
	}

	private fun insertStatus(
		deltakerId: UUID,
	) {
		deltakerStatusRepository.upsert(
			listOf(
				DeltakerStatusDbo(
				deltakerId = deltakerId,
				endretDato = LocalDateTime.now().minusDays(1),
				status = Deltaker.Status.DELTAR,
				aktiv = true
		)))
	}


	private fun insertGjennomforing(tiltakId: UUID, arrangorId: UUID, navn: String = "Kaffekurs"): GjennomforingDbo {
		val id = UUID.randomUUID()
		return gjennomforingRepository.insert(
			id = id,
			tiltakId = tiltakId,
			arrangorId = arrangorId,
			navn = navn,
			status = Gjennomforing.Status.GJENNOMFORES,
			startDato = null,
			sluttDato = null,
			registrertDato = LocalDateTime.now(),
			fremmoteDato = null,
			navEnhetId = null,
			lopenr = 123,
			opprettetAar = 2020
		)
	}

	private fun insertArrangor(orgNavn: String? = "Et Orgnavn"): UUID {
		val id = UUID.randomUUID()
		val orgnr = (1000..9999).random()
		val insert = """
			INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer,navn)
			VALUES ('$id', '12345678', '$orgNavn', '$orgnr', 'Virksomhetsnavn1')
		""".trimIndent()
		namedJdbcTemplate.jdbcTemplate.update(insert)

		return id
	}

}
