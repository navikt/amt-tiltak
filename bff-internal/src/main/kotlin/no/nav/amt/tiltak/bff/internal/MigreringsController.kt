package no.nav.amt.tiltak.bff.internal

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.common.job.JobRunner
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Unprotected
@RestController
@RequestMapping("/internal/api/migrer")
class MigreringsController(
	private val navAnsattService: NavAnsattService,
) {

	@PostMapping("/nav-ansatt")
	fun migrerNavAnsatte(request: HttpServletRequest) {
		if (isInternal(request)) {
			JobRunner.runAsync("migrer_nav_ansatte_til_amt_person", navAnsattService::migrerAlle)
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	private fun isInternal(request: HttpServletRequest): Boolean {
		return request.remoteAddr == "127.0.0.1"
	}

}
