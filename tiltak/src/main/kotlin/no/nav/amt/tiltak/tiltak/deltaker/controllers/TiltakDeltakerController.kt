package no.nav.amt.tiltak.tiltak.deltaker.controllers

import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDeltakerDetaljerDto
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/tiltak-deltaker")
class TiltakDeltakerController(
	private val deltakerPresentationService: TiltakDeltakerPresentationService
) {

	@Protected
	@GetMapping("/{tiltakDeltakerId}")
	fun hentTiltakDeltakerDetaljer(@PathVariable("tiltakDeltakerId") deltakerId: UUID): TiltakDeltakerDetaljerDto {
		return deltakerPresentationService.getDeltakerDetaljerById(deltakerId)
	}

}
