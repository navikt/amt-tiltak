package no.nav.amt.tiltak.deltaker.controllers

import no.nav.amt.tiltak.deltaker.service.EndringsmeldingService
import org.apache.commons.lang3.NotImplementedException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(value = ["/api/nav-ansatt/endringsmelding"])
class EndringsmeldingNavController(
	private val service: EndringsmeldingService
) {

	@GetMapping("/gjennomforing/{gjennomforingId}")
	fun hentEndringsmeldinger(@PathVariable("gjennomforingId") gjennomforingId: UUID) {
		throw NotImplementedException("Not implemented yet")
	}
}
