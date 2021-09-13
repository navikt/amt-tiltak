package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/api/tiltak")
class TiltakController {

	@GetMapping
	fun hentTiltak(@RequestParam tiltaksleverandorId: String): List<TiltakDTO> {
		TODO()
	}

}
