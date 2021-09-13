package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakInstansDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController("/api/tiltak")
class TiltakController {

	@GetMapping("/{tiltakId}/instanser")
	fun hentInstanser(@PathVariable tiltakId: String): List<TiltakInstansDTO> {
		TODO()
	}

}
