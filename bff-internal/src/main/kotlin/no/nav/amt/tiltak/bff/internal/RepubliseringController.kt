package no.nav.amt.tiltak.bff.internal

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.common.job.JobRunner
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Unprotected
@RestController
@RequestMapping("/internal/api/republisering")
class RepubliseringController(
	private val deltakerService: DeltakerService
) {

	@GetMapping("/deltakere")
	fun republiserDeltakere(request: HttpServletRequest) {
		if (isInternal(request)) {
			JobRunner.runAsync("republiser_deltakere_kafka", deltakerService::republiserAlleDeltakerePaKafka)
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	private fun isInternal(request: HttpServletRequest): Boolean {
		return request.remoteAddr == "127.0.0.1"
	}


}
