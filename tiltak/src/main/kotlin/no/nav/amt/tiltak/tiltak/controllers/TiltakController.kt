package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakDto
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakInstansDto
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/tiltak")
class TiltakController {

	@Protected
	@GetMapping
	fun hentAlleTiltak(@RequestParam("tiltaksleverandorId") tiltaksleverandorId: String): List<TiltakInstansDto> {
		return listOf(
			TiltakInstansDto(
				id = UUID.randomUUID(),
				navn = "Truckførerkurs",
				oppstartdato = LocalDate.now().plusDays(1),
				sluttdato = LocalDate.now().plusDays(3),
				status = TiltakInstans.Status.IKKE_STARTET,
				TiltakDto(
					tiltakskode = "GRUPPEAMO",
					tiltaksnavn = "Gruppe AMO"
				),
			),
			TiltakInstansDto(
				id = UUID.randomUUID(),
				navn = "Sveisekurs",
				oppstartdato = LocalDate.now().minusDays(1),
				sluttdato = LocalDate.now().plusDays(8),
				status = TiltakInstans.Status.GJENNOMFORES,
				TiltakDto(
					tiltakskode = "GRUPPEAMO",
					tiltaksnavn = "Gruppe AMO"
				),
			),
			TiltakInstansDto(
				id = UUID.randomUUID(),
				navn = "Jobbklubb",
				oppstartdato = LocalDate.now().minusDays(10),
				sluttdato = LocalDate.now().minusDays(8),
				status = TiltakInstans.Status.AVSLUTTET,
				TiltakDto(
					tiltakskode = "JOBBK",
					tiltaksnavn = "Jobbklubb"
				),
			)
		)
	}

}
