package no.nav.amt.tiltak.tiltak.controllers
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDeltakerDto
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakInstansDto
import no.nav.amt.tiltak.tiltak.controllers.dto.toDto
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

import java.util.*
import kotlin.NoSuchElementException

@RestController
@RequestMapping("/api/tiltak-instans")
class TiltakInstansController(
	private val tiltakService: TiltakService,
	private val deltakerService: DeltakerService
) {

	companion object {
		private val log = LoggerFactory.getLogger(TiltakInstansController::class.java)
	}

	@Protected
	@GetMapping("/{tiltakInstansId}")
	fun hentTiltakInstans(@PathVariable("tiltakInstansId") tiltakInstansId: String): TiltakInstansDto {
		val instansId = UUID.fromString(tiltakInstansId)
		try {
			return tiltakService.getTiltakInstans(instansId).toDto()
		} catch (e: NoSuchElementException) {
			log.error("Fant ikke tiltaksinstans", e)
			throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke tiltakinstans")
		}

	}

	@Protected
	@GetMapping("/{tiltakInstansId}/deltakere")
	fun hentDeltakere(@PathVariable("tiltakInstansId") tiltakInstansId: String): List<TiltakDeltakerDto> {
		val id = UUID.fromString(tiltakInstansId)
		return deltakerService.hentDeltakerePaaTiltak(id)
			.map { it.toDto() }
	}

}
