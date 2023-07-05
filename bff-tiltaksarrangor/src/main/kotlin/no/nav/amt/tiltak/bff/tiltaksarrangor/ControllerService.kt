package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import org.slf4j.MDC
import org.springframework.stereotype.Service

@Service
open class ControllerService(
	private val arrangorAnsattService: ArrangorAnsattService,
	private val authService: AuthService
) {
	fun hentInnloggetAnsatt(): Ansatt {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		return arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent)
			.also { MDC.put("ansatt-id", it?.id.toString()) }
			?: throw UnauthorizedException("Arrangor ansatt finnes ikke")
	}
}
