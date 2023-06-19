package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle.KOORDINATOR
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle.VEILEDER
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.ArrangorVeilederService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor.AmtArrangorService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.UUID

class ArrangorAnsattTilgangServiceImplTest {

	lateinit var arrangorAnsattService: ArrangorAnsattService

	lateinit var ansattRolleService: AnsattRolleService

	lateinit var deltakerService: DeltakerService

	lateinit var arrangorAnsattTilgangServiceImpl: ArrangorAnsattTilgangServiceImpl

	lateinit var mineDeltakerlisterService: MineDeltakerlisterServiceImpl

	lateinit var amtArrangorService: AmtArrangorService

	lateinit var arrangorService: ArrangorService

	lateinit var gjennomforingService: GjennomforingService

	lateinit var arrangorVeilederService: ArrangorVeilederService

	lateinit var publisherService: DataPublisherService

	val personligIdent = "fnr"

	val ansattId = UUID.randomUUID()

	val deltakerId = UUID.randomUUID()

	val gjennomforingId = UUID.randomUUID()

	val arrangorId = UUID.randomUUID()

	val datasource = SingletonPostgresContainer.getDataSource()

	val gjennomforing = Gjennomforing(
		id = gjennomforingId,
		tiltak = Tiltak(
			id = UUID.randomUUID(),
			kode = "TEST",
			navn = "TEST"
		),
		arrangor = Arrangor(
			id = arrangorId,
			navn = "TEST",
			organisasjonsnummer = "123",
			overordnetEnhetNavn = null,
			overordnetEnhetOrganisasjonsnummer = null
		),
		navn = "TEST",
		status = Gjennomforing.Status.GJENNOMFORES,
		startDato = null,
		sluttDato = null,
		navEnhetId = null,
		opprettetAar = 2022,
		lopenr = 3837,
		erKurs = false
	)

	val deltaker = Deltaker(
		id = deltakerId,
		fornavn = "fornavn",
		etternavn = "etternavn",
		personIdent = personligIdent,
		navEnhet = null,
		navVeilederId = null,
		telefonnummer = "123",
		epost = "foo",
		startDato = null,
		sluttDato = null,
		status = DeltakerStatus(
			id = UUID.randomUUID(),
			type =  DeltakerStatus.Type.VENTER_PA_OPPSTART,
			aarsak = null,
			gyldigFra =  LocalDateTime.now().minusHours(1),
			opprettetDato = LocalDateTime.now(),
			aktiv = true
		),
		registrertDato = LocalDateTime.now(),
		dagerPerUke = 5,
		prosentStilling = 100F,
		gjennomforingId = gjennomforingId,
		erSkjermet = true,
		endretDato = LocalDateTime.now()
	)
	@BeforeEach
	fun beforeEach() {
		arrangorAnsattService = mockk()

		ansattRolleService = mockk(relaxUnitFun = true)

		deltakerService = mockk()

		mineDeltakerlisterService = mockk(relaxUnitFun = true)

		amtArrangorService = mockk()

		arrangorService = mockk()

		gjennomforingService = mockk()

		arrangorVeilederService = mockk()

		gjennomforingService = mockk()

		publisherService = mockk()

		arrangorAnsattTilgangServiceImpl = ArrangorAnsattTilgangServiceImpl(
			arrangorAnsattService, ansattRolleService,
			deltakerService, gjennomforingService, mineDeltakerlisterService, arrangorVeilederService,
			arrangorService, TransactionTemplate(DataSourceTransactionManager(datasource)), publisherService, amtArrangorService
		)

		every {
			arrangorAnsattService.getAnsattByPersonligIdent(personligIdent)
		} returns Ansatt(
			ansattId,
			personligIdent,
			"",
			null,
			"",
			emptyList()
		)

		every {
			gjennomforingService.getArrangorId(gjennomforingId)
		} returns arrangorId
	}
	@Test
	fun `verifiserTilgangTilGjennomforing - koordinator har ikke lagt til deltakerliste - skal kaste exception`() {
		every {
			mineDeltakerlisterService.hent(ansattId)
		} returns emptyList()

		every {
			ansattRolleService.hentAktiveRoller(any())
		} returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(KOORDINATOR)
			)
		)

		shouldThrowExactly<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilGjennomforing(ansattId, gjennomforingId)
		}
	}

	@Test
	fun `verifiserTilgangTilGjennomforing - koordinator lagt til deltakerliste - skal ikke kaste exception`() {
		every {
			mineDeltakerlisterService.hent(ansattId)
		} returns listOf(gjennomforingId)

		every {
			ansattRolleService.hentAktiveRoller(any())
		} returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(KOORDINATOR)
			)
		)

		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilGjennomforing(ansattId, gjennomforingId)
		}
	}

	@Test
	fun `verifiserTilgangTilGjennomforing - veileder rolle - skal kaste exception`() {
		every {
			mineDeltakerlisterService.hent(ansattId)
		} returns listOf(gjennomforingId)

		every {
			ansattRolleService.hentAktiveRoller(any())
		} returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(VEILEDER)
			)
		)

		shouldThrowExactly<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilGjennomforing(ansattId, gjennomforingId)
		}
	}

	@Test
	fun `verifiserTilgangTilDeltaker - veileder rolle med tilgang - skal ikke kaste exception`() {
		every {
			arrangorVeilederService.erVeilederFor(ansattId, deltakerId)
		} returns true

		every {
			ansattRolleService.hentAktiveRoller(any())
		} returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(VEILEDER)
			)
		)

		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns deltaker

		shouldNotThrow<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilDeltaker(ansattId, deltakerId)
		}
	}
	@Test
	fun `verifiserTilgangTilDeltaker - veileder uten tilgang til arrangør - skal kaste exception`() {
		every {
			arrangorVeilederService.erVeilederFor(ansattId, deltakerId)
		} returns true

		every {
			ansattRolleService.hentAktiveRoller(any())
		} returns emptyList()

		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns deltaker

		shouldThrowExactly<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilDeltaker(ansattId, deltakerId)
		}

	}

	@Test
	fun `verifiserTilgangTilDeltaker - veileder med tilgang til deltaker, og annen arrangør - skal kaste exception`() {
		every {
			arrangorVeilederService.erVeilederFor(ansattId, deltakerId)
		} returns true

		every {
			ansattRolleService.hentAktiveRoller(any())
		} returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = UUID.randomUUID(),
				roller = listOf(VEILEDER)
			)
		)

		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns deltaker

		shouldThrowExactly<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilDeltaker(ansattId, deltakerId)
		}

	}

	@Test
	fun `verifiserTilgangTilDeltaker - koordinator har tilgang til arrangør - skal ikke kaste exception`() {
		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns deltaker

		every {
			mineDeltakerlisterService.hent(ansattId)
		} returns listOf(gjennomforingId)

		every {
			ansattRolleService.hentAktiveRoller(any())
		} returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(KOORDINATOR)
			)
		)

		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilDeltaker(ansattId, deltakerId)
		}
	}

	@Test
	fun `verifiserTilgangTilDeltaker - koordinator ikke tilgang til arrangør - skal kaste exception`() {
		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns deltaker

		every {
			mineDeltakerlisterService.hent(ansattId)
		} returns emptyList()

		every {
			ansattRolleService.hentAktiveRoller(any())
		} returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = UUID.randomUUID(),
				roller = listOf(KOORDINATOR)
			)
		)

		shouldThrowExactly<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilDeltaker(ansattId, deltakerId)
		}
	}

	@Test
	fun `synkroniserRettigheterMedAltinn - skal legge til nye roller fra Altinn`() {
		val ansattPersonligIdent = "1234"
		val ansattId = UUID.randomUUID()

		val arrangorId = UUID.randomUUID()
		val organisasjonsnummer = "5678"

		every { amtArrangorService.getAnsatt(ansattPersonligIdent) } returns ArrangorAnsatt(
			id = ansattId,
			personalia = ArrangorAnsatt.PersonaliaDto(
				personident = ansattPersonligIdent,
				personId = UUID.randomUUID(),
				navn = ArrangorAnsatt.Navn("", null, "")
			),
			arrangorer = listOf(
				ArrangorAnsatt.TilknyttetArrangorDto(
					arrangorId = arrangorId,
					arrangor = ArrangorAnsatt.Arrangor(
						id = arrangorId,
						navn = "",
						organisasjonsnummer = organisasjonsnummer
					),
					overordnetArrangor = null,
					roller = listOf(ArrangorAnsatt.AnsattRolle.KOORDINATOR),
					veileder = emptyList(),
					koordinator = emptyList()
				)
			)
		)
		every { arrangorAnsattService.upsertAnsatt(match { it.id == ansattId }) } just Runs
		every { ansattRolleService.hentAktiveRoller(ansattId) } returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(VEILEDER)
			)
		)
		every { arrangorService.getOrCreateArrangor(match { it.organisasjonsnummer == organisasjonsnummer }) } returns Arrangor(
			id = arrangorId,
			navn = "",
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = null,
			overordnetEnhetNavn = null,
		)

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 1) { ansattRolleService.opprettRolle(any(), ansattId, arrangorId, KOORDINATOR) }
	}

	@Test
	fun `synkroniserRettigheterMedAltinn - skal ikke legge til rolle hvis allerede finnes`() {
		val ansattPersonligIdent = "1234"
		val ansattId = UUID.randomUUID()

		val arrangorId = UUID.randomUUID()
		val organisasjonsnummer = "5678"

		every { amtArrangorService.getAnsatt(ansattPersonligIdent) } returns ArrangorAnsatt(
			id = ansattId,
			personalia = ArrangorAnsatt.PersonaliaDto(
				personident = ansattPersonligIdent,
				personId = UUID.randomUUID(),
				navn = ArrangorAnsatt.Navn("", null, "")
			),
			arrangorer = listOf(
				ArrangorAnsatt.TilknyttetArrangorDto(
					arrangorId = arrangorId,
					arrangor = ArrangorAnsatt.Arrangor(
						id = arrangorId,
						navn = "",
						organisasjonsnummer = organisasjonsnummer
					),
					overordnetArrangor = null,
					roller = listOf(ArrangorAnsatt.AnsattRolle.KOORDINATOR),
					veileder = emptyList(),
					koordinator = emptyList()
				))
		)
		every { arrangorAnsattService.upsertAnsatt(match { it.id == ansattId }) } just Runs
		every { ansattRolleService.hentAktiveRoller(ansattId) } returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(KOORDINATOR)
			)
		)
		every { arrangorService.getOrCreateArrangor(match { it.organisasjonsnummer == organisasjonsnummer }) } returns Arrangor(
			id = arrangorId,
			navn = "",
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = null,
			overordnetEnhetNavn = null,
		)

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 0) { ansattRolleService.opprettRolle(any(), any(), any(), any()) }
	}

	@Test
	fun `synkroniserRettigheterMedAltinn - skal fjerne roller som ikke finnes i Altinn`() {
		val ansattPersonligIdent = "1234"
		val ansattId = UUID.randomUUID()

		val arrangorId = UUID.randomUUID()
		val organisasjonsnummer = "5678"

		val arrangorId2 = UUID.randomUUID()
		val organisasjonsnummer2 = "9999"
		every { amtArrangorService.getAnsatt(ansattPersonligIdent) } returns ArrangorAnsatt(
			id = ansattId,
			personalia = ArrangorAnsatt.PersonaliaDto(
				personident = ansattPersonligIdent,
				personId = UUID.randomUUID(),
				navn = ArrangorAnsatt.Navn("", null, "")
			),
			arrangorer = listOf(
				ArrangorAnsatt.TilknyttetArrangorDto(
					arrangorId = arrangorId2,
					arrangor = ArrangorAnsatt.Arrangor(
						id = arrangorId2,
						navn = "",
						organisasjonsnummer = organisasjonsnummer2
					),
					overordnetArrangor = null,
					roller = listOf(ArrangorAnsatt.AnsattRolle.KOORDINATOR, ArrangorAnsatt.AnsattRolle.VEILEDER),
					veileder = emptyList(),
					koordinator = emptyList()
				))
		)
		every { arrangorAnsattService.upsertAnsatt(match { it.id == ansattId }) } just Runs
		every { ansattRolleService.hentAktiveRoller(ansattId) } returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(KOORDINATOR, VEILEDER)
			)
		)
		every { arrangorService.getOrCreateArrangor(match { it.organisasjonsnummer == organisasjonsnummer2 }) } returns Arrangor(
			id = arrangorId2,
			navn = "",
			organisasjonsnummer = organisasjonsnummer2,
			overordnetEnhetOrganisasjonsnummer = null,
			overordnetEnhetNavn = null,
		)
		every { arrangorService.getOrCreateArrangor(match { it.organisasjonsnummer == organisasjonsnummer }) } returns Arrangor(
			id = arrangorId,
			navn = "",
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = null,
			overordnetEnhetNavn = null,
		)

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 1) { ansattRolleService.deaktiverRolleHosArrangor(ansattId, arrangorId, KOORDINATOR) }
		verify(exactly = 1) { ansattRolleService.deaktiverRolleHosArrangor(ansattId, arrangorId, VEILEDER) }
		verify(exactly = 1) { mineDeltakerlisterService.fjernAlleHosArrangor(ansattId, arrangorId) }
		verify(exactly = 1) { arrangorVeilederService.fjernAlleDeltakereForVeilederHosArrangor(ansattId, arrangorId) }
	}

	@Test
	fun `synkroniserRettigheterMedAltinn - skal returne tidlig hvis ikke ansatt`() {
		val ansattPersonligIdent = "1234"

		every { amtArrangorService.getAnsatt(ansattPersonligIdent) } returns null

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 0) { ansattRolleService.opprettRolle(any(), any(), any(), any()) }
		verify(exactly = 0) { ansattRolleService.deaktiverRolleHosArrangor(any(), any(), any()) }
		verify(exactly = 0) { mineDeltakerlisterService.fjernAlleHosArrangor(any(), any()) }
		verify(exactly = 0) { arrangorVeilederService.fjernAlleDeltakereForVeilederHosArrangor(any(), any()) }
	}

	@Test
	fun `synkroniserRettigheterMedAltinn - skal returne tidlig hvis ingen roller`() {
		val ansattPersonligIdent = "1234"

		every { amtArrangorService.getAnsatt(ansattPersonligIdent) } returns ArrangorAnsatt(
			id = UUID.randomUUID(),
			personalia = ArrangorAnsatt.PersonaliaDto(
				personident = ansattPersonligIdent,
				personId = UUID.randomUUID(),
				navn = ArrangorAnsatt.Navn("", null, "")
			),
			arrangorer = emptyList()
		)

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 0) { ansattRolleService.opprettRolle(any(), any(), any(), any()) }
		verify(exactly = 0) { ansattRolleService.deaktiverRolleHosArrangor(any(), any(), any()) }
		verify(exactly = 0) { mineDeltakerlisterService.fjernAlleHosArrangor(any(), any()) }
		verify(exactly = 0) { arrangorVeilederService.fjernAlleDeltakereForVeilederHosArrangor(any(), any()) }
	}
}
