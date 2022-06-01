package no.nav.amt.tiltak.nav_enhet

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.common.job.JobRunner
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/internal/nav-enhet")
open class InternalNavEnhetController(
	private val authService: AuthService,
	private val publiserNavEnhetService: PubliserNavEnhetService
) {

	@Unprotected
	@PostMapping("/publiser-alle-enheter")
	fun publiserAlleEnheter(httpServletRequest: HttpServletRequest) {
		if (!authService.isInternalRequest(httpServletRequest)) {
			throw UnauthorizedException("Not an internal request")
		}

		JobRunner.runAsync("publiser_alle_nav_enheter", publiserNavEnhetService::publiserAlleNavEnheter)
	}

}
