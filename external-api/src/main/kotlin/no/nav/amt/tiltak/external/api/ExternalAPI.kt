package no.nav.amt.tiltak.external.api

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.external.api.dto.ArrangorDto
import no.nav.amt.tiltak.external.api.dto.DeltakerDto
import no.nav.amt.tiltak.external.api.dto.GjennomforingDto
import no.nav.amt.tiltak.external.api.dto.HarAktiveDeltakelserResponse
import no.nav.amt.tiltak.external.api.dto.toDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/external")
class ExternalAPI(
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val authService: AuthService
	) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/mine-deltakelser")
	fun hentMineDeltakelser(): List<DeltakerDto> {
		val personIdent = authService.hentPersonligIdentTilInnloggetBruker()
		return hentDeltakelser(personIdent)
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PostMapping("/deltakelser")
	fun hentDeltakelserForPerson(@RequestBody body: HentDeltakelserRequest): List<DeltakerDto> {
		authService.validerErM2MToken()
		return hentDeltakelser(body.personIdent)
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PostMapping("/aktiv-deltaker")
	fun harAktiveDeltakelser(@RequestBody body: HentDeltakelserRequest): HarAktiveDeltakelserResponse {
		authService.validerErM2MToken()
		val harAktiveDeltakelser = harAktiveDeltakelser(hentDeltakelser(body.personIdent))
		return HarAktiveDeltakelserResponse(harAktiveDeltakelser)
	}

	private fun hentDeltakelser(personIdent: String): List<DeltakerDto> {
		val deltakere = deltakerService.hentDeltakereMedPersonIdent(personIdent)
		val gjennomforinger = deltakere.distinctBy { it.gjennomforingId }
			.map { gjennomforingService.getGjennomforing(it.gjennomforingId) }
			.associateBy { it.id }

		return deltakere.map { it.toDto(gjennomforinger[it.gjennomforingId]!!) }
	}

	private fun harAktiveDeltakelser(deltakelser: List<DeltakerDto>): Boolean {
		if (deltakelser.isEmpty()) {
			return false
		}
		return deltakelser.find { it.erAktiv() } != null
	}
}

data class HentDeltakelserRequest(
	val personIdent: String
)

fun Deltaker.toDto(gjennomforing: Gjennomforing) = DeltakerDto(
	id = id,
	gjennomforing = gjennomforing.toDto(),
	startDato = startDato,
	sluttDato = sluttDato,
	status = status.toDto(),
	dagerPerUke = dagerPerUke,
	prosentStilling = prosentStilling,
	registrertDato = registrertDato,
)

fun Gjennomforing.toDto() = GjennomforingDto(
	id = id,
	navn = navn,
	type = tiltak.kode,
	tiltakstypeNavn = tiltak.navn,
	arrangor = ArrangorDto(
		navn = arrangor.navn,
		virksomhetsnummer = arrangor.organisasjonsnummer
	)
)
