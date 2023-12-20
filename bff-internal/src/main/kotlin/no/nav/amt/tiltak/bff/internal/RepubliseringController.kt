package no.nav.amt.tiltak.bff.internal

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.common.job.JobRunner
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Unprotected
@RestController
@RequestMapping("/internal/api/republisering")
class RepubliseringController(
	private val deltakerService: DeltakerService,
	private val dataPublisher: DataPublisherService
) {

	@GetMapping("/deltakere")
	fun republiserDeltakere(request: HttpServletRequest) {
		if (isInternal(request)) {
			JobRunner.runAsync("republiser_deltakere_kafka", deltakerService::republiserAlleDeltakerePaKafka)
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	@GetMapping("/deltakere/{id}")
	fun republiserDeltaker(
		@PathVariable("id") id: UUID,
		request: HttpServletRequest,
	) {
		if (isInternal(request)) {
			JobRunner.runAsync("republiser_deltaker_kafka") { deltakerService.republiserDeltakerPaKafka(id) }
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	@PostMapping
	fun republishAll(request: HttpServletRequest) {
		if (isInternal(request)) {
			JobRunner.runAsync("republiser_all_data_til_kafka", dataPublisher::publishAll)
		} else {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}
	}

	@PostMapping("/{type}")
	fun republishType(
		@PathVariable("type") type: DataPublishType,
		request: HttpServletRequest
	) {
		if (isInternal(request)) {
			JobRunner.runAsync("republiser_${type}") { dataPublisher.publish(type = type) }
		}
	}


	@PostMapping("/{type}/{id}")
	fun republish(
		@PathVariable("type") type: DataPublishType,
		@PathVariable("id") id: UUID,
		request: HttpServletRequest
	) {
		if (isInternal(request)) {
			JobRunner.runAsync("republiser_${type}_${id}") { dataPublisher.publish(id, type) }
		}
	}

	private fun isInternal(request: HttpServletRequest): Boolean {
		return request.remoteAddr == "127.0.0.1"
	}


}
