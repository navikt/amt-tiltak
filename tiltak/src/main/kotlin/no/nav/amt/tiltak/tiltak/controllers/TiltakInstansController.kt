package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDeltagerDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController("/api/tiltak-instans")
class TiltakInstansController {

	@GetMapping("/{tiltkInstansId}/deltagere")
	fun hentDeltagere(@PathVariable("tiltkInstansId") tiltkInstansId: String): List<TiltakDeltagerDto> {
		TODO()
	}

}
