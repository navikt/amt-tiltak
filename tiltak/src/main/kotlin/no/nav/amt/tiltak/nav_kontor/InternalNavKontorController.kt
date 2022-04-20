package no.nav.amt.tiltak.nav_kontor

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.common.job.JobRunner
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/internal/nav-kontor")
open class InternalNavKontorController(
	private val authService: AuthService,
	private val publiserNavKontorService: PubliserNavKontorService
) {

	@Unprotected
	@PostMapping("/publiser-alle-enheter")
	fun publiserAlleEnheter(httpServletRequest: HttpServletRequest) {
		if (!authService.isInternalRequest(httpServletRequest)) {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not an internal request")
		}

		JobRunner.runAsync("publiser_alle_nav_enheter", publiserNavKontorService::publiserAlleNavEnheter)
	}

}