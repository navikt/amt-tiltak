package no.nav.amt.tiltak.bff.internal

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.BrukerService
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
	private val brukerService: BrukerService,
	private val navAnsattService: NavAnsattService,
	private val arrangorAnsattService: ArrangorAnsattService,
) {

	@PostMapping("/nav-bruker")
	fun migrerNavBrukere(request: HttpServletRequest) {
		if (isInternal(request)) {
			JobRunner.runAsync("migrer_nav_brukere_til_amt_person", brukerService::migrerAlle)
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	@PostMapping("/nav-ansatt")
	fun migrerNavAnsatte(request: HttpServletRequest) {
		if (isInternal(request)) {
			JobRunner.runAsync("migrer_nav_ansatte_til_amt_person", navAnsattService::migrerAlle)
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	@PostMapping("/arrangor-ansatt")
	fun migrerArrangorAnsatte(request: HttpServletRequest) {
		if (isInternal(request)) {
			JobRunner.runAsync("migrer_arrangor_ansatte_til_amt_person", arrangorAnsattService::migrerAlle)
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	private fun isInternal(request: HttpServletRequest): Boolean {
		return request.remoteAddr == "127.0.0.1"
	}

}
