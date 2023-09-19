package no.nav.amt.tiltak.bff.internal

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.common.job.JobRunner
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Unprotected
@RestController
@RequestMapping("/internal/api/endringsmelding")
class EndringsmeldingController(
	private val endringsmeldingService: EndringsmeldingService
) {

	@DeleteMapping("/er-aktuell")
	fun republiserDeltakere(request: HttpServletRequest) {
		if (isInternal(request)) {
			JobRunner.runAsync("slett_em_aktuell", endringsmeldingService::slettErAktuell)
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	@DeleteMapping("/er-ikke-aktuell")
	fun republiserDeltaker(request: HttpServletRequest) {
		if (isInternal(request)) {
			JobRunner.runAsync("slett_em_ikke_aktuell") { endringsmeldingService.slettErIkkeAktuellOppfyllerIkkeKravene() }
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	private fun isInternal(request: HttpServletRequest): Boolean {
		return request.remoteAddr == "127.0.0.1"
	}


}
