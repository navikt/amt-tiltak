package no.nav.amt.tiltak.bff.internal

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Unprotected
@RestController
@RequestMapping("/internal/api")
class InternalController(
	val amtArrangorClient: AmtArrangorClient,
	val gjennomforingService: GjennomforingService,
	val deltakerService: DeltakerService,
) {
	private val log = LoggerFactory.getLogger(javaClass)

	@PostMapping("/fjern-tilganger")
	fun fjernTilgangerHosArrangor(
		request: HttpServletRequest,
		@RequestBody body: FjernTilgangerHosArrangorRequest
	) {
		if (!isInternal(request)) {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}

		val gjennomforing = gjennomforingService.getGjennomforing(body.deltakerlisteId)
		if (gjennomforing.arrangor.id == body.arrangorId) {
			throw IllegalStateException("Kan ikke fjerne tilganger som ligger hos nåværende arrangør")
		}

		val deltakerIder = deltakerService.hentDeltakerePaaGjennomforing(gjennomforing.id).map { it.id }

		amtArrangorClient.fjernTilganger(body.arrangorId, body.deltakerlisteId, deltakerIder)

	}

	@DeleteMapping("/deltaker/{id}")
	fun slettDeltaker(
		request: HttpServletRequest,
		@PathVariable("id") id: UUID
	) {
		if (!isInternal(request)) {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
		deltakerService.slettDeltaker(id)
		log.info("Slettet deltaker med id $id")
	}

	@DeleteMapping("/deltakerliste/{id}")
	fun slettDeltakerliste(
		request: HttpServletRequest,
		@PathVariable("id") id: UUID
	) {
		if (!isInternal(request)) {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
		val deltakere = deltakerService.hentDeltakerePaaGjennomforing(id)
		if (deltakere.isNotEmpty()) {
			throw IllegalStateException("Kan ikke slette gjennomføring som har deltakere")
		}
		gjennomforingService.slettGjennomforing(id)
		log.info("Slettet deltakerliste med id $id")
	}


	private fun isInternal(request: HttpServletRequest): Boolean {
		return request.remoteAddr == "127.0.0.1"
	}

	data class FjernTilgangerHosArrangorRequest(
		val arrangorId: UUID,
		val deltakerlisteId: UUID,
	)
}
