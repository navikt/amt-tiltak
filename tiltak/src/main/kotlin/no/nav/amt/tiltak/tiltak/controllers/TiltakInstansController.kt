package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDeltakerDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController("/api/tiltak-instans")
class TiltakInstansController {

	@GetMapping("/{tiltkInstansId}/deltakere")
	fun hentDeltakere(@PathVariable("tiltkInstansId") tiltkInstansId: String): List<TiltakDeltakerDTO> {
		TODO()
	}

}
