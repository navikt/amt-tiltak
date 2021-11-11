package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDeltagerDto
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDto
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakInstansDto
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/tiltak-instans")
class TiltakInstansController {

	@Protected
	@GetMapping("/{tiltakInstansId}")
	fun hentTiltakInstans(@PathVariable("tiltakInstansId") tiltakInstansId: String): TiltakInstansDto {
		return TiltakInstansDto(
			id = UUID.randomUUID(),
			navn = "Truckførerkurs",
			startdato = ZonedDateTime.now().plusDays(1),
			sluttdato = ZonedDateTime.now().plusDays(3),
			status = TiltakInstans.Status.IKKE_STARTET,
			deltagerAntall = 8,
			deltagerKapasitet = 4,
			TiltakDto(
				tiltakskode = "GRUPPEAMO",
				tiltaksnavn = "Gruppe AMO"
			),
		)
	}

	@Protected
	@GetMapping("/{tiltkInstansId}/deltagere")
	fun hentDeltagere(@PathVariable("tiltkInstansId") tiltkInstansId: String): List<TiltakDeltagerDto> {
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
