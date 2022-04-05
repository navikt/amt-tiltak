package no.nav.amt.tiltak.deltaker.controllers

import no.nav.amt.tiltak.deltaker.service.EndringsmeldingService
import org.apache.commons.lang3.NotImplementedException
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/endringsmelding")
class EndringsmeldingNavController(
	private val service: EndringsmeldingService
) {

	@GetMapping
	fun hentEndringsmeldinger(@RequestParam("gjennomforingId") gjennomforingId: UUID) {
		throw NotImplementedException("Not implemented yet")
	}
}
