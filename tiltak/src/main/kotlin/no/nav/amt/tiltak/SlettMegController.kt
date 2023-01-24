package no.nav.amt.tiltak

import no.nav.amt.tiltak.deltaker.service.BrukerService
import no.nav.common.job.JobRunner
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/internal/diskresjonskode")
class SlettMegController(
	private val service: BrukerService
) {

	@Unprotected
	@GetMapping
	fun logBrukereMedAdressebeskyttelse(
		servlet: HttpServletRequest
	) {
		if (isInternal(servlet)) {
			JobRunner.runAsync("log_brukere_med_diskresjonskode") {
				service.logSkjermedeBrukere()
			}
		}
	}

	private fun isInternal(servlet: HttpServletRequest): Boolean {
		return servlet.remoteAddr == "127.0.0.1"
	}

}
