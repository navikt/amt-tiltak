package no.nav.amt.tiltak.tilgangskontroll.tilgang

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tilgangskontroll.altinn.AltinnService
import no.nav.amt.tiltak.tilgangskontroll.altinn.Rettighet
import org.springframework.http.HttpStatus
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime
import java.util.*

class ArrangorAnsattTilgangServiceImplTest : FunSpec({

	lateinit var arrangorAnsattService: ArrangorAnsattService

	lateinit var ansattRolleService: AnsattRolleService

	lateinit var deltakerService: DeltakerService

	lateinit var arrangorAnsattTilgangServiceImpl: ArrangorAnsattTilgangServiceImpl

	lateinit var arrangorAnsattGjennomforingTilgangService: ArrangorAnsattGjennomforingTilgangService

	lateinit var altinnService: AltinnService

	lateinit var arrangorService: ArrangorService

	val personligIdent = "fnr"

	val ansattId = UUID.randomUUID()

	val gjennomforingId = UUID.randomUUID()

	val arrangorId = UUID.randomUUID()

	val datasource = SingletonPostgresContainer.getDataSource()

	beforeEach {
		arrangorAnsattService = mockk()

		ansattRolleService = mockk(relaxUnitFun = true)

		deltakerService = mockk()

		arrangorAnsattGjennomforingTilgangService = mockk(relaxUnitFun = true)

		altinnService = mockk()

		arrangorService = mockk()

		arrangorAnsattTilgangServiceImpl = ArrangorAnsattTilgangServiceImpl(
			arrangorAnsattService, ansattRolleService,
			deltakerService, altinnService, arrangorAnsattGjennomforingTilgangService,
			arrangorService, TransactionTemplate(DataSourceTransactionManager(datasource))
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
	}

	test("verifiserTilgangTilGjennomforing skal kaste exception hvis ikke tilgang") {
		every {
			arrangorAnsattGjennomforingTilgangService.hentGjennomforingerForAnsatt(ansattId)
		} returns emptyList()

		val exception = shouldThrowExactly<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilGjennomforing(personligIdent, gjennomforingId)
		}

		exception.status shouldBe HttpStatus.FORBIDDEN
	}

	test("verifiserTilgangTilGjennomforing skal ikke kaste exception hvis tilgang") {
		every {
			arrangorAnsattGjennomforingTilgangService.hentGjennomforingerForAnsatt(ansattId)
		} returns listOf(gjennomforingId)

		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilGjennomforing(personligIdent, gjennomforingId)
		}
	}

	test("verifiserTilgangTilArrangor skal kaste exception hvis ikke tilgang") {
		every {
			ansattRolleService.hentArrangorIderForAnsatt(ansattId)
		} returns listOf(UUID.randomUUID())

		val exception = shouldThrowExactly<ResponseStatusException> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilArrangor(personligIdent, arrangorId)
		}

		exception.status shouldBe HttpStatus.FORBIDDEN
	}

	test("verifiserTilgangTilArrangor skal ikke kaste exception hvis tilgang") {
		every {
			ansattRolleService.hentArrangorIderForAnsatt(ansattId)
		} returns listOf(arrangorId)

		shouldNotThrow<Throwable> {
			arrangorAnsattTilgangServiceImpl.verifiserTilgangTilArrangor(personligIdent, arrangorId)
		}
	}

	test("synkroniserRettigheterMedAltinn - skal legge til nye roller fra Altinn") {
		val ansattPersonligIdent = "1234"
		val ansattId = UUID.randomUUID()

		val arrangorId = UUID.randomUUID()
		val organisasjonsnummer = "5678"

		every { altinnService.hentAltinnRettigheter(ansattPersonligIdent) } returns listOf(
			Rettighet(AnsattRolle.KOORDINATOR, organisasjonsnummer)
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
			AnsattRolleDbo(
				id = UUID.randomUUID(),
				ansattId = ansattId,
				arrangorId = arrangorId,
				rolle = AnsattRolle.VEILEDER,
				createdAt = ZonedDateTime.now(),
				gyldigFra = ZonedDateTime.now(),
				gyldigTil = ZonedDateTime.now(),
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

		verify(exactly = 1) { ansattRolleService.opprettRolle(any(), ansattId, arrangorId, AnsattRolle.KOORDINATOR) }
	}

	test("synkroniserRettigheterMedAltinn - skal ikke legge til rolle hvis allerede finnes") {
		val ansattPersonligIdent = "1234"
		val ansattId = UUID.randomUUID()

		val arrangorId = UUID.randomUUID()
		val organisasjonsnummer = "5678"

		every { altinnService.hentAltinnRettigheter(ansattPersonligIdent) } returns listOf(
			Rettighet(AnsattRolle.KOORDINATOR, organisasjonsnummer)
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
			AnsattRolleDbo(
				id = UUID.randomUUID(),
				ansattId = ansattId,
				arrangorId = arrangorId,
				rolle = AnsattRolle.KOORDINATOR,
				createdAt = ZonedDateTime.now(),
				gyldigFra = ZonedDateTime.now(),
				gyldigTil = ZonedDateTime.now(),
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
		every { altinnService.hentAltinnRettigheter(ansattPersonligIdent) } returns listOf(
			Rettighet(AnsattRolle.KOORDINATOR, organisasjonsnummer2)
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
			AnsattRolleDbo(
				id = UUID.randomUUID(),
				ansattId = ansattId,
				arrangorId = arrangorId,
				rolle = AnsattRolle.KOORDINATOR,
				createdAt = ZonedDateTime.now(),
				gyldigFra = ZonedDateTime.now(),
				gyldigTil = ZonedDateTime.now(),
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

		verify(exactly = 1) { ansattRolleService.deaktiverRolleHosArrangor(ansattId, arrangorId, AnsattRolle.KOORDINATOR) }
		verify(exactly = 1) { arrangorAnsattGjennomforingTilgangService.fjernTilgangTilGjennomforinger(ansattId, arrangorId) }

	}

	test("synkroniserRettigheterMedAltinn - skal returne tidlig hvis ingen rolle og ikke ansatt") {
		val ansattPersonligIdent = "1234"
		val ansattId = UUID.randomUUID()

		val arrangorId = UUID.randomUUID()
		val organisasjonsnummer = "5678"

		every { altinnService.hentAltinnRettigheter(ansattPersonligIdent) } returns listOf()
		every { arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent) } returns null

		arrangorAnsattTilgangServiceImpl.synkroniserRettigheterMedAltinn(ansattPersonligIdent)

		verify(exactly = 0) { ansattRolleService.opprettRolle(any(), any(), any(), any()) }
		verify(exactly = 0) { ansattRolleService.deaktiverRolleHosArrangor(any(), any(), any()) }
		verify(exactly = 0) { arrangorAnsattGjennomforingTilgangService.fjernTilgangTilGjennomforinger(any(), any()) }
	}
})
