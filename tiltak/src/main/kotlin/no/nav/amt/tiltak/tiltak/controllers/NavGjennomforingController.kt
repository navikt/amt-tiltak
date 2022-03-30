package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.tiltak.dto.GjennomforingDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = [ "/api/nav-ansatt/gjennomforing" ])
class NavGjennomforingController(
	private val authService: AuthService,
	private val navAnsattService: NavAnsattService,
	private val gjennomforingService: GjennomforingService
) {

	private val log = LoggerFactory.getLogger(javaClass)

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping
	fun hentGjennomforinger(): List<GjennomforingDto> {
		val navIdent = authService.navIdent() // Todo - flytte disse kallene inn i AuthService?
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)
		log.info("Henter gjennomføringer for nav-ansatt(${navIdent}, ${navAnsatt.navn}. Ikke implementert enda")


		// feature toggle/guard until finished
		if(System.getenv("NAIS_CLUSTER_NAME") != "dev-gcp") return emptyList()

		// Når enhetene hentes fra arena gjennomforingService.getForEnheter(emptyList<String>())
		return emptyList()
	}


}
