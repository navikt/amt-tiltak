package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle.KOORDINATOR
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.altinn.AltinnService
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.altinn.ArrangorAnsattRoller
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*

class ArrangorAnsattTilgangServiceImplTest : FunSpec({

	lateinit var arrangorAnsattService: ArrangorAnsattService

	lateinit var ansattRolleService: AnsattRolleService

	lateinit var deltakerService: DeltakerService

	lateinit var arrangorAnsattTilgangServiceImpl: ArrangorAnsattTilgangServiceImpl

	lateinit var arrangorAnsattGjennomforingTilgangService: ArrangorAnsattGjennomforingTilgangService

	lateinit var altinnService: AltinnService

	lateinit var arrangorService: ArrangorService

	lateinit var gjennomforingService: GjennomforingService

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
		lopenr = 3837
	)

	val deltaker = Deltaker(
		id = deltakerId,
		fornavn = "fornavn",
		etternavn = "etternavn",
		personIdent = personligIdent,
		navEnhetId = null,
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
		erSkjermet = true
	)
	beforeEach {
		arrangorAnsattService = mockk()

		ansattRolleService = mockk(relaxUnitFun = true)

		deltakerService = mockk()

		arrangorAnsattGjennomforingTilgangService = mockk(relaxUnitFun = true)

		altinnService = mockk()

		arrangorService = mockk()

		gjennomforingService = mockk()

		arrangorAnsattTilgangServiceImpl = ArrangorAnsattTilgangServiceImpl(
			arrangorAnsattService, ansattRolleService,
			deltakerService, altinnService, arrangorAnsattGjennomforingTilgangService,
			arrangorService, TransactionTemplate(DataSourceTransactionManager(datasource)),
			gjennomforingService
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
			ansattRolleService.hentAktiveRoller(any())
		} returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(KOORDINATOR)
			)
		)

		every {
			gjennomforingService.getGjennomforing(any())
		} returns gjennomforing
	}

	test("verifiserTilgangTilGjennomforing skal kaste exception hvis ikke tilgang") {
		every {
			arrangorAnsattGjennomforingTilgangService.hentGjennomforingerForAnsatt(ansattId)
		} returns emptyList()

		shouldThrowExactly<UnauthorizedException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilGjennomforing(personligIdent, gjennomforingId, KOORDINATOR)
		}
	}

	test("verifiserTilgangTilGjennomforing skal ikke kaste exception hvis tilgang") {
		every {
			arrangorAnsattGjennomforingTilgangService.hentGjennomforingerForAnsatt(ansattId)
		} returns listOf(gjennomforingId)

		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilGjennomforing(personligIdent, gjennomforingId, KOORDINATOR)
		}
	}

	test("verifiserTilgangTilArrangor skal kaste exception hvis ikke tilgang") {
		every {
			ansattRolleService.hentArrangorIderForAnsatt(ansattId)
		} returns listOf(UUID.randomUUID())

		shouldThrowExactly<UnauthorizedException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilArrangor(personligIdent, arrangorId, KOORDINATOR)
		}
	}

	test("verifiserTilgangTilArrangor skal ikke kaste exception hvis tilgang") {
		every {
			ansattRolleService.hentArrangorIderForAnsatt(ansattId)
		} returns listOf(arrangorId)

		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilArrangor(personligIdent, arrangorId, KOORDINATOR)
		}
	}

	test("verifiserTilgangTilDeltaker skal ikke kaste exception hvis tilgang") {
		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns deltaker

		every {
			arrangorAnsattGjennomforingTilgangService.hentGjennomforingerForAnsatt(ansattId)
		} returns listOf(gjennomforingId)

		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilDeltaker(ansattId, deltakerId, KOORDINATOR)
		}
	}

	test("verifiserTilgangTilDeltaker skal kaste exception hvis ikke tilgang til gjennomforing") {
		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns deltaker

		every {
			arrangorAnsattGjennomforingTilgangService.hentGjennomforingerForAnsatt(ansattId)
		} returns emptyList()

		shouldThrowExactly<UnauthorizedException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilDeltaker(ansattId, deltakerId, KOORDINATOR)
		}
	}

	test("verifiserAnsatteHarRolleHosArrangorer - ansatt har rolle hos arrangorer - skal ikke kaste exception") {
		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl
				.verifiserAnsatteHarRolleHosArrangorer(listOf(ansattId), listOf(arrangorId), KOORDINATOR)
		}
	}

	test("verifiserAnsatteHarRolleHosArrangorer - ansatt har rolle hos arrangorer - skal kaste exception hvis ikke") {
		shouldThrowExactly<UnauthorizedException> {
			arrangorAnsattTilgangServiceImpl
				.verifiserAnsatteHarRolleHosArrangorer(listOf(ansattId), listOf(UUID.randomUUID()), KOORDINATOR)
		}
	}

	test("synkroniserRettigheterMedAltinn - skal legge til nye roller fra Altinn") {
		val ansattPersonligIdent = "1234"
		val ansattId = UUID.randomUUID()

		val arrangorId = UUID.randomUUID()
		val organisasjonsnummer = "5678"

		every { altinnService.hentTiltaksarrangorRoller(ansattPersonligIdent) } returns listOf(
			ArrangorAnsattRoller(organisasjonsnummer, listOf(KOORDINATOR))
		)
		every { arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent) } returns null
		every { arrangorAnsattService.opprettAnsattHvisIkkeFinnes(ansattPersonligIdent) } returns Ansatt(
			id = ansattId,
			personligIdent = ansattPersonligIdent,
			fornavn = "",
			mellomnavn = null,
			etternavn = "",
			arrangorer = emptyList(),
		)
		every { ansattRolleService.hentAktiveRoller(ansattId) } returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(ArrangorAnsattRolle.VEILEDER)
			)
		)
		every { arrangorService.getOrCreateArrangor(organisasjonsnummer) } returns Arrangor(
			id = arrangorId,
			navn = "",
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = null,
			overordnetEnhetNavn = null,
		)

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 1) { ansattRolleService.opprettRolle(any(), ansattId, arrangorId, KOORDINATOR) }
	}

	test("synkroniserRettigheterMedAltinn - skal ikke legge til rolle hvis allerede finnes") {
		val ansattPersonligIdent = "1234"
		val ansattId = UUID.randomUUID()

		val arrangorId = UUID.randomUUID()
		val organisasjonsnummer = "5678"

		every { altinnService.hentTiltaksarrangorRoller(ansattPersonligIdent) } returns listOf(
			ArrangorAnsattRoller(organisasjonsnummer, listOf(KOORDINATOR))
		)
		every { arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent) } returns null
		every { arrangorAnsattService.opprettAnsattHvisIkkeFinnes(ansattPersonligIdent) } returns Ansatt(
			id = ansattId,
			personligIdent = ansattPersonligIdent,
			fornavn = "",
			mellomnavn = null,
			etternavn = "",
			arrangorer = emptyList(),
		)
		every { ansattRolleService.hentAktiveRoller(ansattId) } returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(KOORDINATOR)
			)
		)
		every { arrangorService.getOrCreateArrangor(organisasjonsnummer) } returns Arrangor(
			id = arrangorId,
			navn = "",
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = null,
			overordnetEnhetNavn = null,
		)

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 0) { ansattRolleService.opprettRolle(any(), any(), any(), any()) }
	}

	test("synkroniserRettigheterMedAltinn - skal fjerne roller som ikke finnes i Altinn") {
	val ansattPersonligIdent = "1234"
		val ansattId = UUID.randomUUID()

		val arrangorId = UUID.randomUUID()
		val organisasjonsnummer = "5678"

		val organisasjonsnummer2 = "9999"
		every { altinnService.hentTiltaksarrangorRoller(ansattPersonligIdent) } returns listOf(
			ArrangorAnsattRoller(organisasjonsnummer2, listOf(KOORDINATOR))
		)
		every { arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent) } returns null
		every { arrangorAnsattService.opprettAnsattHvisIkkeFinnes(ansattPersonligIdent) } returns Ansatt(
			id = ansattId,
			personligIdent = ansattPersonligIdent,
			fornavn = "",
			mellomnavn = null,
			etternavn = "",
			arrangorer = emptyList(),
		)
		every { ansattRolleService.hentAktiveRoller(ansattId) } returns listOf(
			no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller(
				arrangorId = arrangorId,
				roller = listOf(KOORDINATOR)
			)
		)
		every { arrangorService.getOrCreateArrangor(organisasjonsnummer2) } returns Arrangor(
			id = UUID.randomUUID(),
			navn = "",
			organisasjonsnummer = organisasjonsnummer2,
			overordnetEnhetOrganisasjonsnummer = null,
			overordnetEnhetNavn = null,
		)
		every { arrangorService.getOrCreateArrangor(organisasjonsnummer) } returns Arrangor(
			id = arrangorId,
			navn = "",
			organisasjonsnummer = organisasjonsnummer,
			overordnetEnhetOrganisasjonsnummer = null,
			overordnetEnhetNavn = null,
		)

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 1) { ansattRolleService.deaktiverRolleHosArrangor(ansattId, arrangorId, KOORDINATOR) }
		verify(exactly = 1) { arrangorAnsattGjennomforingTilgangService.fjernTilgangTilGjennomforinger(ansattId, arrangorId) }

	}

	test("synkroniserRettigheterMedAltinn - skal returne tidlig hvis ingen rolle og ikke ansatt") {
		val ansattPersonligIdent = "1234"

		every { altinnService.hentTiltaksarrangorRoller(ansattPersonligIdent) } returns listOf()
		every { arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent) } returns null

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 0) { ansattRolleService.opprettRolle(any(), any(), any(), any()) }
		verify(exactly = 0) { ansattRolleService.deaktiverRolleHosArrangor(any(), any(), any()) }
		verify(exactly = 0) { arrangorAnsattGjennomforingTilgangService.fjernTilgangTilGjennomforinger(any(), any()) }
	}
})
