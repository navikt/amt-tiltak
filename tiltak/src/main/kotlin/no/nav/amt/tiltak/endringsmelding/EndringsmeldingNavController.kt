package no.nav.amt.tiltak.endringsmelding
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.deltaker.dto.EndringsmeldingDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/endringsmelding")
class EndringsmeldingNavController(
	private val service: EndringsmeldingService,
	private val navAnsattService: NavAnsattService,
	private val authService: AuthService
) {
	private val log = LoggerFactory.getLogger(javaClass)


	@GetMapping
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentEndringsmeldinger(@RequestParam("gjennomforingId") gjennomforingId: UUID) : List<EndringsmeldingDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)
		log.info("Henter endringsmeldinger for nav-ansatt(${navIdent}, ${navAnsatt.navn}. Ikke implementert enda")

		// Når enhetene hentes fra arena: return service.hentEndringsmeldinger(gjennomforingId).map { it.toDto() }
		return emptyList()

	}
}
