package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.TiltakInstansService
import no.nav.amt.tiltak.tiltak.dto.TiltakDeltakerDto
import no.nav.amt.tiltak.tiltak.dto.TiltakInstansDto
import no.nav.amt.tiltak.tiltak.dto.toDto
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/tiltak-instans")
class TiltakInstansController(
	private val tiltakInstansService: TiltakInstansService,
	private val deltakerService: DeltakerService
) {

	private val log = LoggerFactory.getLogger(javaClass)

	@Protected
	@GetMapping
	fun hentTiltakInstanserByArrangorId(@RequestParam("arrangorId") arrangorId: UUID): List<TiltakInstansDto> {
		return tiltakInstansService.getTiltakInstanserForArrangor(arrangorId)
			.map { it.toDto() }
	}

	@Protected
	@GetMapping("/{tiltakInstansId}")
	fun hentTiltakInstans(@PathVariable("tiltakInstansId") tiltakInstansId: UUID): TiltakInstansDto {
		try {
			return tiltakInstansService.getTiltakInstans(tiltakInstansId).toDto()
		} catch (e: NoSuchElementException) {
			log.error("Fant ikke tiltaksinstans", e)
			throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke tiltakinstans")
		}

	}

	@Protected
	@GetMapping("/{tiltakInstansId}/deltakere")
	fun hentDeltakere(@PathVariable("tiltakInstansId") tiltakInstansId: UUID): List<TiltakDeltakerDto> {
		return deltakerService.hentDeltakerePaaTiltakInstans(tiltakInstansId)
			.map { it.toDto() }
	}

}
