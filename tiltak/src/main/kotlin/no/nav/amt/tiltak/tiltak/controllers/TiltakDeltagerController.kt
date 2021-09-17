package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDeltagerDetaljerDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController("/api/tiltak-deltager/")
class TiltakDeltagerController {

	@GetMapping("/{tiltakDeltagerId}")
	fun hentTiltakDeltagerDetaljer(@PathVariable("tiltakDeltagerId") tiltakDeltagerId: String): TiltakDeltagerDetaljerDto {
		TODO()
	}

}
