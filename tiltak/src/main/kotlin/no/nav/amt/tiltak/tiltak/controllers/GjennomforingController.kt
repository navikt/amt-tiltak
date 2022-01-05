package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.tiltak.dto.TiltakDeltakerDto
import no.nav.amt.tiltak.tiltak.dto.GjennomforingDto
import no.nav.amt.tiltak.tiltak.dto.toDto
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/gjennomforing")
class GjennomforingController(
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService
) {

	private val log = LoggerFactory.getLogger(javaClass)

	@Protected
	@GetMapping
	fun hentGjennomforingerByArrangorId(@RequestParam("arrangorId") arrangorId: UUID): List<GjennomforingDto> {
		return gjennomforingService.getGjennomforingerForArrangor(arrangorId)
			.map { it.toDto() }
	}

	@Protected
	@GetMapping("/{gjennomforingId}")
	fun hentGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID): GjennomforingDto {
		try {
			return gjennomforingService.getGjennomforing(gjennomforingId).toDto()
		} catch (e: NoSuchElementException) {
			log.error("Fant ikke gjennomforing", e)
			throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke gjennomforingId")
		}

	}

	@Protected
	@GetMapping("/{gjennomforingId}/deltakere")
	fun hentDeltakere(@PathVariable("gjennomforingId") gjennomforingId: UUID): List<TiltakDeltakerDto> {
		return deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId)
			.map { it.toDto() }
	}

}
