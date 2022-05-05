package no.nav.amt.tiltak.endringsmelding
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.amt.tiltak.deltaker.dto.EndringsmeldingDto
import no.nav.amt.tiltak.deltaker.dto.toDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/endringsmelding")
class EndringsmeldingNavController(
	private val service: EndringsmeldingService,
	private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService,
	private val authService: AuthService
) {

	@GetMapping
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentEndringsmeldinger(@RequestParam("gjennomforingId") gjennomforingId: UUID) : List<EndringsmeldingDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		if (!tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		return service.hentEndringsmeldinger(gjennomforingId).map { it.toDto() }
	}

}
