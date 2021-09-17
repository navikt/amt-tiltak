package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDto
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakInstansDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@RestController("/api/tiltak")
class TiltakController {

	@GetMapping
	fun hentAlleTiltak(@RequestParam("tiltaksleverandorId") tiltaksleverandorId: String): List<TiltakInstansDto> {
		return listOf(
			TiltakInstansDto(
				id = UUID.randomUUID(),
				navn = "Truckførerkurs",
				startdato = ZonedDateTime.now().plusDays(1),
				sluttdato = ZonedDateTime.now().plusDays(3),
				status = TiltakInstans.Status.IKKE_STARTET,
				antallDeltagere = 8,
				deltagerKapasitet = 4,
				TiltakDto(
					tiltakskode = "GRUPPEAMO",
					tiltaksnavn = "Gruppe AMO"
				),
			),
			TiltakInstansDto(
				id = UUID.randomUUID(),
				navn = "Sveisekurs",
				startdato = ZonedDateTime.now().minusDays(1),
				sluttdato = ZonedDateTime.now().plusDays(8),
				status = TiltakInstans.Status.GJENNOMFORES,
				antallDeltagere = 5,
				deltagerKapasitet = 8,
				TiltakDto(
					tiltakskode = "GRUPPEAMO",
					tiltaksnavn = "Gruppe AMO"
				),
			),
			TiltakInstansDto(
				id = UUID.randomUUID(),
				navn = "Jobbklubb",
				startdato = ZonedDateTime.now().minusDays(10),
				sluttdato = ZonedDateTime.now().minusDays(8),
				status = TiltakInstans.Status.AVSLUTTET,
				antallDeltagere = 5,
				deltagerKapasitet = 8,
				TiltakDto(
					tiltakskode = "JOBBK",
					tiltaksnavn = "Jobbklubb"
				),
			)
		)
	}

}
