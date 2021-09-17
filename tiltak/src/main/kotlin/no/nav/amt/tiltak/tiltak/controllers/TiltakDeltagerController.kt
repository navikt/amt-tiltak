package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.domain.tiltak.Deltager
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.controllers.dto.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@RestController("/api/tiltak-deltager/")
class TiltakDeltagerController {

	@GetMapping("/{tiltakDeltagerId}")
	fun hentTiltakDeltagerDetaljer(@PathVariable("tiltakDeltagerId") tiltakDeltagerId: String): TiltakDeltagerDetaljerDto {
		return TiltakDeltagerDetaljerDto(
			id = UUID.randomUUID(),
			fornavn = "Stødig",
			mellomnavn = "Mektig",
			etternavn = "Bord",
			fodselsdato = LocalDate.of(2001, 6, 8),
			startdato = ZonedDateTime.now().minusDays(1),
			sluttdato = ZonedDateTime.now().plusDays(1),
			status = Deltager.Status.GJENNOMFORES,
			navVeileder = NavVeilederDTO(
				navn = "Veileder Neilederesen",
				telefon = "12345678",
				epost = "veileder@testnav.no"
			),
			navKontor = NavKontorDTO(
				navn = "NAV Testheim",
				adresse = "Gamle Navvei 14"
			),
			epost = "Stødig@bord.com",
			telefon = "87654321",
			tiltakInstans = TiltakInstansDto(
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
				)
			)
		)
	}

}
