package no.nav.amt.tiltak.bff.nav_ansatt

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.bff.nav_ansatt.dto.DeltakerDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_SKJERMET
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.createDeltakerInput
import no.nav.amt.tiltak.test.database.data.TestData.createStatusInput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

class EndringsmeldingControllerTest {
	val endringsmeldingService = mockk<EndringsmeldingService>()
	val deltakerService = mockk<DeltakerService>()
	val navAnsattService = mockk<NavAnsattService>()
	val taAuthService = mockk<TiltaksansvarligAutoriseringService>()
	val authService = mockk<AuthService>()
	val controller = EndringsmeldingController(
		endringsmeldingService,
		navAnsattService,
		deltakerService,
		taAuthService,
		authService
	);
	val navAnsattAzureId = UUID.randomUUID()
	val navIdent = "z1232"

	@BeforeEach
	fun before() {

		every { authService.hentAzureIdTilInnloggetBruker() } returns navAnsattAzureId
		every { authService.hentNavIdentTilInnloggetBruker() } returns navIdent
		every { taAuthService.harTilgangTilSkjermedePersoner(navAnsattAzureId) } returns false
		every { taAuthService.verifiserTilgangTilEndringsmelding(navAnsattAzureId) } returns Unit

	}

	@Test
	fun `hentEndringsmeldinger - deltaker er ikke skjermet, nav ansatt har ikke tilgang til skjermede - returnerer umaskert bruker`() {
		val gjennomforingId = GJENNOMFORING_1.id
		val endringsmelding = Endringsmelding(
			id = UUID.randomUUID(),
			deltakerId = DELTAKER_1.id,
			utfortAvNavAnsattId = UUID.randomUUID(),
			utfortTidspunkt = ZonedDateTime.now(),
			opprettetAvArrangorAnsattId = UUID.randomUUID(),
			opprettet = ZonedDateTime.now(),
			status = Endringsmelding.Status.AKTIV,
			innhold = Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold(LocalDate.now()),
		)

		val deltaker = DELTAKER_1.toDeltaker(BRUKER_1, DELTAKER_1_STATUS_1)
		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { taAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId) } returns Unit

		val endringsmeldingerResult = controller.hentEndringsmeldinger(gjennomforingId)

		endringsmeldingerResult.size shouldBe 1
		endringsmeldingerResult[0].deltaker shouldBe DeltakerDto(
			fornavn = BRUKER_1.fornavn,
			mellomnavn = BRUKER_1.mellomnavn,
			etternavn = BRUKER_1.etternavn,
			fodselsnummer = BRUKER_1.fodselsnummer,
			erSkjermet = BRUKER_1.erSkjermet
		)
	}

	@Test
	fun `hentEndringsmeldinger - skjermet deltaker, nav-ansatt mangler tilgang til skjermede - returnerer maskert bruker`() {
		val gjennomforingId = GJENNOMFORING_1.id
		val deltakerInput = createDeltakerInput(BRUKER_SKJERMET, GJENNOMFORING_1)
		val deltaker = deltakerInput.toDeltaker(BRUKER_SKJERMET, createStatusInput(deltakerInput))

		val endringsmelding = Endringsmelding(
			id = UUID.randomUUID(),
			deltakerId = deltakerInput.id,
			utfortAvNavAnsattId = UUID.randomUUID(),
			utfortTidspunkt = ZonedDateTime.now(),
			opprettetAvArrangorAnsattId = UUID.randomUUID(),
			opprettet = ZonedDateTime.now(),
			status = Endringsmelding.Status.AKTIV,
			innhold = Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold(LocalDate.now()),
		)

		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { taAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId) } returns Unit

		val endringsmeldingerResult = controller.hentEndringsmeldinger(gjennomforingId)

		endringsmeldingerResult.size shouldBe 1
		endringsmeldingerResult.get(0).deltaker shouldBe DeltakerDto(
			fornavn = null,
			mellomnavn = null,
			etternavn = null,
			fodselsnummer = null,
			erSkjermet = BRUKER_SKJERMET.erSkjermet
		)
	}

	@Test
	fun `hentEndringsmeldinger - skjermet deltaker, nav-ansatt har tilgang til skjermede - returnerer umaskert bruker`() {
		val gjennomforingId = GJENNOMFORING_1.id
		val deltakerInput = createDeltakerInput(BRUKER_SKJERMET, GJENNOMFORING_1)
		val deltaker = deltakerInput.toDeltaker(BRUKER_SKJERMET, createStatusInput(deltakerInput))

		val endringsmelding = Endringsmelding(
			id = UUID.randomUUID(),
			deltakerId = deltakerInput.id,
			utfortAvNavAnsattId = UUID.randomUUID(),
			utfortTidspunkt = ZonedDateTime.now(),
			opprettetAvArrangorAnsattId = UUID.randomUUID(),
			opprettet = ZonedDateTime.now(),
			status = Endringsmelding.Status.AKTIV,
			innhold = Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold(LocalDate.now()),
		)

		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { taAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId) } returns Unit
		every { taAuthService.harTilgangTilSkjermedePersoner(navAnsattAzureId) } returns true

		val endringsmeldingerResult = controller.hentEndringsmeldinger(gjennomforingId)

		endringsmeldingerResult.size shouldBe 1
		endringsmeldingerResult.get(0).deltaker shouldBe DeltakerDto(
			fornavn = BRUKER_SKJERMET.fornavn,
			mellomnavn = BRUKER_SKJERMET.mellomnavn,
			etternavn = BRUKER_SKJERMET.etternavn,
			fodselsnummer = BRUKER_SKJERMET.fodselsnummer,
			erSkjermet = BRUKER_SKJERMET.erSkjermet
		)
	}
}
