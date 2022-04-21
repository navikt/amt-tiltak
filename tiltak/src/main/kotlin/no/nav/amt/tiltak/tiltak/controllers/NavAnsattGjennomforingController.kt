package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingerPaEnheterQuery
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/gjennomforing")
class NavAnsattGjennomforingController(
	private val authService: AuthService,
	private val navAnsattService: NavAnsattService,
	private val gjennomforingerPaEnheterQuery: GjennomforingerPaEnheterQuery,
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping
	fun hentGjennomforinger(): List<HentAlleGjennomforingDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val enheter = navAnsattService.hentTiltaksansvarligEnhetTilganger(navIdent)

		return gjennomforingerPaEnheterQuery.query(enheter.map { it.kontor.id })
			.map { HentAlleGjennomforingDto(it.id, it.navn) }
	}

	data class HentAlleGjennomforingDto(
		val id: UUID,
		val navn: String,
	)

}
