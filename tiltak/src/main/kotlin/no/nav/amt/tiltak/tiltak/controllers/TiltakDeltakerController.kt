package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDeltakerDetaljerDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController("/api/tiltak-deltaker/")
class TiltakDeltakerController {

	@GetMapping("/{tiltakDeltakerId}")
	fun hentTiltakDeltakerDetaljer(@PathVariable("tiltakDeltakerId") tiltakDeltakerId: String): TiltakDeltakerDetaljerDto {
		TODO()
	}

}
