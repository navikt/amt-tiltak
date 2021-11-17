package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDeltagerDto
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakInstansDto
import no.nav.amt.tiltak.tiltak.controllers.dto.toDto
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/tiltak-instans")
class TiltakInstansController(
	private val tiltakService: TiltakService,
) {

	@Protected
	@GetMapping("/{tiltakInstansId}")
	fun hentTiltakInstans(@PathVariable("tiltakInstansId") tiltakInstansId: String): TiltakInstansDto {
		val instansId = UUID.fromString(tiltakInstansId)
		val instans = tiltakService.getTiltakInstans(instansId)?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke tiltakinstans")
		val tiltak = tiltakService.getTiltak(instans.tiltakId)?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fant ikke tiltak")
		//Hvordan sjekke om innlogget bruker har tilgang til tiltaksgjennomføring?

		return instans.toDto(tiltak);

	}

	@Protected
	@GetMapping("/{tiltakInstansId}/deltagere")
	fun hentDeltagere(@PathVariable("tiltakInstansId") tiltkInstansId: String): List<TiltakDeltagerDto> {
		return listOf(
			TiltakDeltagerDto(
				id = UUID.randomUUID(),
				fornavn = "Stødig",
				mellomnavn = "Mektig",
				etternavn = "Bord",
				fodselsdato = LocalDate.of(2001, 6, 8),
				startdato = ZonedDateTime.now().minusDays(1),
				sluttdato = ZonedDateTime.now().plusDays(1),
				status = Deltaker.Status.GJENNOMFORES
			),
			TiltakDeltagerDto(
				id = UUID.randomUUID(),
				fornavn = "Prektig",
				mellomnavn = "",
				etternavn = "Telefon",
				fodselsdato = LocalDate.of(2001, 6, 8),
				startdato = ZonedDateTime.now().plusDays(1),
				sluttdato = ZonedDateTime.now().plusDays(4),
				status = Deltaker.Status.GJENNOMFORES
			),
		)
	}

}
