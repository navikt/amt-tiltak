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
import no.nav.amt.tiltak.external.api.dto.toDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/external")
class ExternalController(
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
	@GetMapping("/deltakelser")
	fun hentDeltakelserForPerson(@RequestParam("personIdent") personIdent: String): List<DeltakerDto> {
		authService.validerErM2MToken()
		return hentDeltakelser(personIdent)
	}

	private fun hentDeltakelser(personIdent: String): List<DeltakerDto> {
		return deltakerService.hentDeltakereMedPersonIdent(personIdent)
			.map {
					deltaker -> deltaker.toDto(gjennomforingService.getGjennomforing(deltaker.gjennomforingId))
			}
	}
}

fun Deltaker.toDto(gjennomforing: Gjennomforing) = DeltakerDto(
	id = id,
	gjennomforing = gjennomforing.toDto(),
	startDato = startDato,
	sluttDato = sluttDato,
	status = status.toDto(),
	dagerPerUke = dagerPerUke?.toFloat(),
	prosentStilling = prosentStilling,
	registrertDato = registrertDato,
)

fun Gjennomforing.toDto() = GjennomforingDto(
	id = id,
	navn = navn,
	type = tiltak.kode,
	arrangor = ArrangorDto(
		navn = arrangor.navn,
		virksomhetsnummer = arrangor.organisasjonsnummer
	)
)
