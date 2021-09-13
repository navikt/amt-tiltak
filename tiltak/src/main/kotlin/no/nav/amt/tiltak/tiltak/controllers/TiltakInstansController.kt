package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakInstansDetaljerDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController("/api/tiltak-instans")
class TiltakInstansController {

	@GetMapping("/{tiltakInstansId}")
	fun hentInstansDetaljer(@PathVariable tiltakInstansId: String): TiltakInstansDetaljerDTO {
		TODO()
	}

}
