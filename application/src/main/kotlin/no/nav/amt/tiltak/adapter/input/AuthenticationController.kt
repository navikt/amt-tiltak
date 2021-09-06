package no.nav.amt.tiltak.adapter.input

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/authentication")
class AuthenticationController {

	@GetMapping("/tiltaksveileder")
	fun hentInnloggetTiltaksveileder(): InnloggetTiltaksveileder {

		// TODO: Hent info direkte fra token eller fra auth-proxy

		return InnloggetTiltaksveileder(
			navn = "TODO",
			tilgjengeligeBedrifter = emptyList()
		)
	}

	data class InnloggetTiltaksveileder(
		val navn: String,
		val tilgjengeligeBedrifter: List<Bedrift>,
	)

	data class Bedrift(
		val navn: String,
		val organisasjonsnummer: String,
		val bedriftsnummer: String,
	)

}
