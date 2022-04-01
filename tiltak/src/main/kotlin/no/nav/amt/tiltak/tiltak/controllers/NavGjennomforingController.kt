package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.tiltak.dto.GjennomforingDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = [ "/api/nav-ansatt/gjennomforing" ])
class NavGjennomforingController() {

	private val log = LoggerFactory.getLogger(javaClass)

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping
	fun hentGjennomforinger(): List<GjennomforingDto> {
		log.info("Henter gjennomføringer for nav-ansatt. Ikke implementert enda")
		return emptyList()
	}


}
