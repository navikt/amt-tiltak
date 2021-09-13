package no.nav.amt.tiltak.tiltaksleverandor.controllers

import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.TiltakDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController("/api/tiltaksleverandor")
class TiltaksleverandorController {

	@GetMapping("/{tiltaksleverandorId}/tiltak")
	fun hentTiltak(@PathVariable tiltaksleverandorId: String): List<TiltakDTO> {
		TODO()
	}

}
