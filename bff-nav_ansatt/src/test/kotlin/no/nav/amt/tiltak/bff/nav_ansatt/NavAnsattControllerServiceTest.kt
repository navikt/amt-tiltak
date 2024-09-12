package no.nav.amt.tiltak.bff.nav_ansatt

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.bff.nav_ansatt.NavAnsattControllerService.Companion.harTilgangTilDeltaker
import no.nav.amt.tiltak.bff.nav_ansatt.dto.DeltakerDto
import no.nav.amt.tiltak.common.auth.AdGruppe
import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.amt.tiltak.core.port.UnleashService
import no.nav.amt.tiltak.core.port.VurderingService
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_ADRESSEBESKYTTET
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_SKJERMET
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestData.createDeltakerInput
import no.nav.amt.tiltak.test.database.data.TestData.createStatusInput
import no.nav.amt.tiltak.test.database.data.inputs.BrukerInput
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

class NavAnsattControllerServiceTest {
	private val endringsmeldingService = mockk<EndringsmeldingService>()
	private val deltakerService = mockk<DeltakerService>()
	private val taAuthService = mockk<TiltaksansvarligAutoriseringService>()
	private val gjennomforingService = mockk<GjennomforingService>()
	private val vurderingService = mockk<VurderingService>()
	private val unleashService = mockk<UnleashService>()
	private val controller = NavAnsattControllerService(
		endringsmeldingService,
		deltakerService,
		gjennomforingService,
		vurderingService,
		unleashService
	)
	private val navIdent = "z1232"

	private val tilgangTilLosning = listOf(
		AdGruppe.TILTAKSANSVARLIG_FLATE_GRUPPE,
		AdGruppe.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE
	)
	private val tilgangTilSkjermede = tilgangTilLosning.plus(AdGruppe.TILTAKSANSVARLIG_EGNE_ANSATTE_GRUPPE)
	private val tilgangTilStrengtFortrolig = tilgangTilLosning.plus(AdGruppe.TILTAKSANSVARLIG_STRENGT_FORTROLIG_ADRESSE_GRUPPE)
	private val tilgangTilFortrolig = tilgangTilLosning.plus(AdGruppe.TILTAKSANSVARLIG_FORTROLIG_ADRESSE_GRUPPE)
	private val tilgangTilAlt = AdGruppe.entries.toList()

	private val brukereMedAdressebeskyttelse = listOf(
		BRUKER_ADRESSEBESKYTTET,
		BRUKER_ADRESSEBESKYTTET.copy(adressebeskyttelse = Adressebeskyttelse.STRENGT_FORTROLIG_UTLAND),
		BRUKER_ADRESSEBESKYTTET.copy(adressebeskyttelse = Adressebeskyttelse.FORTROLIG)
	)

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
			type = Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO
		)

		val deltaker = DELTAKER_1.toDeltaker(BRUKER_1, DELTAKER_1_STATUS_1)
		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		every { gjennomforingService.getGjennomforing(DELTAKER_1.gjennomforingId) } returns GJENNOMFORING_1
			.toGjennomforing(
				TILTAK_1.copy(type = "ARBFORB").toTiltak(),
				ARRANGOR_1.toArrangor()
			)

		val endringsmeldingerResult = controller.hentEndringsmeldinger(gjennomforingId, tilgangTilLosning)

		endringsmeldingerResult.size shouldBe 1
		endringsmeldingerResult[0].deltaker shouldBe umaskertDeltakerDto(BRUKER_1)
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
			type = Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO
		)

		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { taAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId) } returns Unit
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		every { gjennomforingService.getGjennomforing(DELTAKER_1.gjennomforingId) } returns GJENNOMFORING_1
			.toGjennomforing(
				TILTAK_1.copy(type = "ARBFORB").toTiltak(),
				ARRANGOR_1.toArrangor()
			)

		val endringsmeldingerResult = controller.hentEndringsmeldinger(gjennomforingId, tilgangTilLosning)

		endringsmeldingerResult.size shouldBe 1
		endringsmeldingerResult[0].deltaker shouldBe maskertDeltakerDto(BRUKER_SKJERMET)
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
			type = Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO
		)

		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { taAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId) } returns Unit
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		every { gjennomforingService.getGjennomforing(DELTAKER_1.gjennomforingId) } returns GJENNOMFORING_1
			.toGjennomforing(
				TILTAK_1.copy(type = "ARBFORB").toTiltak(),
				ARRANGOR_1.toArrangor()
			)

		val endringsmeldingerResult = controller.hentEndringsmeldinger(gjennomforingId, tilgangTilSkjermede)

		endringsmeldingerResult.size shouldBe 1
		endringsmeldingerResult.get(0).deltaker shouldBe umaskertDeltakerDto(BRUKER_SKJERMET)
	}

	@Test
	fun `hentMeldinger - deltaker er ikke skjermet, nav ansatt har ikke tilgang til skjermede - returnerer umaskert bruker`() {
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
			type = Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO
		)
		val vurdering = Vurdering(
			id = UUID.randomUUID(),
			deltakerId = DELTAKER_1.id,
			vurderingstype = Vurderingstype.OPPFYLLER_KRAVENE,
			begrunnelse = null,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			gyldigFra = LocalDateTime.now(),
			gyldigTil = null
		)

		val deltaker = DELTAKER_1.toDeltaker(BRUKER_1, DELTAKER_1_STATUS_1)
		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { vurderingService.hentAktiveVurderingerForGjennomforing(gjennomforingId) } returns listOf(vurdering)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		every { gjennomforingService.getGjennomforing(DELTAKER_1.gjennomforingId) } returns GJENNOMFORING_1
			.toGjennomforing(
				TILTAK_1.copy(type = "ARBFORB").toTiltak(),
				ARRANGOR_1.toArrangor()
			)
		val forventetDeltaker = umaskertDeltakerDto(BRUKER_1)

		val meldingerFraArrangorResponse = controller.hentMeldinger(gjennomforingId, tilgangTilLosning)

		meldingerFraArrangorResponse.endringsmeldinger.size shouldBe 1
		meldingerFraArrangorResponse.endringsmeldinger[0].deltaker shouldBe forventetDeltaker
		meldingerFraArrangorResponse.vurderinger.size shouldBe 1
		meldingerFraArrangorResponse.vurderinger[0].deltaker shouldBe forventetDeltaker
		meldingerFraArrangorResponse.vurderinger[0].vurderingstype shouldBe Vurderingstype.OPPFYLLER_KRAVENE
	}

	@Test
	fun `hentMeldinger - skjermet deltaker, nav-ansatt mangler tilgang til skjermede - returnerer maskert bruker`() {
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
			type = Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO
		)
		val vurdering = Vurdering(
			id = UUID.randomUUID(),
			deltakerId = deltakerInput.id,
			vurderingstype = Vurderingstype.OPPFYLLER_KRAVENE,
			begrunnelse = null,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			gyldigFra = LocalDateTime.now(),
			gyldigTil = null
		)

		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { vurderingService.hentAktiveVurderingerForGjennomforing(gjennomforingId) } returns listOf(vurdering)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { taAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId) } returns Unit
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		every { gjennomforingService.getGjennomforing(DELTAKER_1.gjennomforingId) } returns GJENNOMFORING_1
			.toGjennomforing(
				TILTAK_1.copy(type = "ARBFORB").toTiltak(),
				ARRANGOR_1.toArrangor()
			)
		val forventetDeltaker = maskertDeltakerDto(BRUKER_SKJERMET)

		val meldingerFraArrangorResponse = controller.hentMeldinger(gjennomforingId, tilgangTilLosning)

		meldingerFraArrangorResponse.endringsmeldinger.size shouldBe 1
		meldingerFraArrangorResponse.endringsmeldinger[0].deltaker shouldBe forventetDeltaker
		meldingerFraArrangorResponse.vurderinger.size shouldBe 1
		meldingerFraArrangorResponse.vurderinger[0].deltaker shouldBe forventetDeltaker
		meldingerFraArrangorResponse.vurderinger[0].vurderingstype shouldBe Vurderingstype.OPPFYLLER_KRAVENE
	}

	@Test
	fun `hentMeldinger - skjermet deltaker, nav-ansatt har tilgang til skjermede - returnerer umaskert bruker`() {
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
			type = Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO
		)
		val vurdering = Vurdering(
			id = UUID.randomUUID(),
			deltakerId = deltakerInput.id,
			vurderingstype = Vurderingstype.OPPFYLLER_KRAVENE,
			begrunnelse = null,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			gyldigFra = LocalDateTime.now(),
			gyldigTil = null
		)

		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { vurderingService.hentAktiveVurderingerForGjennomforing(gjennomforingId) } returns listOf(vurdering)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { taAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId) } returns Unit
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		every { gjennomforingService.getGjennomforing(DELTAKER_1.gjennomforingId) } returns GJENNOMFORING_1
			.toGjennomforing(
				TILTAK_1.copy(type = "ARBFORB").toTiltak(),
				ARRANGOR_1.toArrangor()
			)
		val forventetDeltaker = umaskertDeltakerDto(BRUKER_SKJERMET)

		val meldingerFraArrangorResponse = controller.hentMeldinger(gjennomforingId, tilgangTilSkjermede)

		meldingerFraArrangorResponse.endringsmeldinger.size shouldBe 1
		meldingerFraArrangorResponse.endringsmeldinger[0].deltaker shouldBe forventetDeltaker
		meldingerFraArrangorResponse.vurderinger.size shouldBe 1
		meldingerFraArrangorResponse.vurderinger[0].deltaker shouldBe forventetDeltaker
		meldingerFraArrangorResponse.vurderinger[0].vurderingstype shouldBe Vurderingstype.OPPFYLLER_KRAVENE
	}

	@Test
	fun `hentMeldinger - adressebeskyttede deltakere, ikke tilgang - returnerer maskert bruker`() {
		brukereMedAdressebeskyttelse.forEach {
			testTilganger(it) { gjennomforingId ->
				val meldingerFraArrangorResponse = controller.hentMeldinger(gjennomforingId, tilgangTilLosning)
				meldingerFraArrangorResponse.endringsmeldinger[0].deltaker shouldBe maskertDeltakerDto(it)
				meldingerFraArrangorResponse.vurderinger[0].deltaker shouldBe maskertDeltakerDto(it)
			}
		}
	}

	@Test
	fun `hentEndringsmeldinger - adressebeskyttede deltakere, ikke tilgang - returnerer maskert bruker`() {
		brukereMedAdressebeskyttelse.forEach {
			testTilganger(it) { gjennomforingId ->
				val meldingerFraArrangorResponse = controller.hentEndringsmeldinger(gjennomforingId, tilgangTilLosning)
				meldingerFraArrangorResponse[0].deltaker shouldBe maskertDeltakerDto(it)
			}
		}
	}
	@Test
	fun `hentMeldinger - strengt fortrolig og skjermet deltaker, mangler tilgang til skjermet - returnerer maskert bruker`() {
		val bruker = BRUKER_ADRESSEBESKYTTET.copy(erSkjermet = true)
		testTilganger(bruker) { gjennomforingId ->
			val meldingerFraArrangorResponse = controller.hentMeldinger(gjennomforingId, tilgangTilStrengtFortrolig)
			meldingerFraArrangorResponse.endringsmeldinger[0].deltaker shouldBe maskertDeltakerDto(bruker)
			meldingerFraArrangorResponse.vurderinger[0].deltaker shouldBe maskertDeltakerDto(bruker)
		}
	}

	@Test
	fun `hentMeldinger - strengt fortrolig deltaker, har tilgang - returnerer umaskert bruker`() {
		testTilganger(BRUKER_ADRESSEBESKYTTET) { gjennomforingId ->
			val meldingerFraArrangorResponse = controller.hentMeldinger(gjennomforingId, tilgangTilStrengtFortrolig)
			meldingerFraArrangorResponse.endringsmeldinger[0].deltaker shouldBe umaskertDeltakerDto(BRUKER_ADRESSEBESKYTTET)
			meldingerFraArrangorResponse.vurderinger[0].deltaker shouldBe umaskertDeltakerDto(BRUKER_ADRESSEBESKYTTET)
		}
	}

	@Test
	fun `harTilgangTilDeltaker - skal sjekke om ad-grupper gir tilgang til deltaker`() {
		val deltakereMedAdressebeskyttelse = brukereMedAdressebeskyttelse.map {
			val deltakerInput = createDeltakerInput(it, GJENNOMFORING_1)
			deltakerInput.toDeltaker(it, createStatusInput(deltakerInput))
		}

		deltakereMedAdressebeskyttelse.forEach {
			harTilgangTilDeltaker(it, tilgangTilLosning) shouldBe false
			harTilgangTilDeltaker(it, tilgangTilSkjermede) shouldBe false
			harTilgangTilDeltaker(it, tilgangTilAlt) shouldBe true

			when (it.adressebeskyttelse) {
				Adressebeskyttelse.STRENGT_FORTROLIG_UTLAND,
				Adressebeskyttelse.STRENGT_FORTROLIG -> {
					harTilgangTilDeltaker(it, tilgangTilStrengtFortrolig) shouldBe true
					harTilgangTilDeltaker(it, tilgangTilFortrolig) shouldBe false
				}
				Adressebeskyttelse.FORTROLIG -> {
					harTilgangTilDeltaker(it, tilgangTilStrengtFortrolig) shouldBe false
					harTilgangTilDeltaker(it, tilgangTilFortrolig) shouldBe true
				}
				null -> { /* Skjer ikke */ }
			}
		}
	}

	@Test
	fun `hentEndringsmeldinger - meldinger for AFT håndteres i ny løsning - returnerer ikke endringsmelding`() {
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
			type = Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO
		)

		every { gjennomforingService.getGjennomforing(DELTAKER_1.gjennomforingId) } returns GJENNOMFORING_1
			.toGjennomforing(
				TILTAK_1.copy(type = "ARBFORB").toTiltak(),
				ARRANGOR_1.toArrangor()
			)
		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { deltakerService.hentDeltakerMap(emptyList()) } returns emptyMap()
		every { taAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId) } returns Unit
		every { unleashService.erKometMasterForTiltakstype(any()) } returns true

		val endringsmeldingerResult = controller.hentEndringsmeldinger(gjennomforingId, tilgangTilLosning)

		endringsmeldingerResult.size shouldBe 0
	}

	private fun testTilganger(bruker: BrukerInput, assertions: (gjennomforingId: UUID) -> Unit) {
		val gjennomforingId = GJENNOMFORING_1.id
		val deltakerInput = createDeltakerInput(bruker, GJENNOMFORING_1)
		val deltaker = deltakerInput.toDeltaker(bruker, createStatusInput(deltakerInput))

		val endringsmelding = Endringsmelding(
			id = UUID.randomUUID(),
			deltakerId = deltaker.id,
			utfortAvNavAnsattId = UUID.randomUUID(),
			utfortTidspunkt = ZonedDateTime.now(),
			opprettetAvArrangorAnsattId = UUID.randomUUID(),
			opprettet = ZonedDateTime.now(),
			status = Endringsmelding.Status.AKTIV,
			innhold = Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold(LocalDate.now()),
			type = Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO
		)
		val vurdering = Vurdering(
			id = UUID.randomUUID(),
			deltakerId = deltaker.id,
			vurderingstype = Vurderingstype.OPPFYLLER_KRAVENE,
			begrunnelse = null,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			gyldigFra = LocalDateTime.now(),
			gyldigTil = null
		)

		every { endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId) } returns listOf(endringsmelding)
		every { vurderingService.hentAktiveVurderingerForGjennomforing(gjennomforingId) } returns listOf(vurdering)
		every { deltakerService.hentDeltakerMap(listOf(deltaker.id)) } returns mapOf(endringsmelding.deltakerId to deltaker)
		every { gjennomforingService.getGjennomforing(DELTAKER_1.gjennomforingId) } returns GJENNOMFORING_1
			.toGjennomforing(
				TILTAK_1.copy(type = "ARBFORB").toTiltak(),
				ARRANGOR_1.toArrangor()
			)
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false

		assertions(gjennomforingId)
	}

	private fun umaskertDeltakerDto(bruker: BrukerInput) = DeltakerDto(
		fornavn = bruker.fornavn,
		mellomnavn = bruker.mellomnavn,
		etternavn = bruker.etternavn,
		fodselsnummer = bruker.personIdent,
		erSkjermet = bruker.erSkjermet,
		adressebeskyttelse = bruker.adressebeskyttelse,
	)

	private fun maskertDeltakerDto(it: BrukerInput) = DeltakerDto(
		fornavn = null,
		mellomnavn = null,
		etternavn = null,
		fodselsnummer = null,
		erSkjermet = it.erSkjermet,
		adressebeskyttelse = it.adressebeskyttelse,
	)
}
