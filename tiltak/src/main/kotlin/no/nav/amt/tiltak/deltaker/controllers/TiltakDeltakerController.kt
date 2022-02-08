package no.nav.amt.tiltak.deltaker.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.tiltak.dto.TiltakDeltakerDetaljerDto
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/tiltak-deltaker")
class TiltakDeltakerController(
	private val deltakerPresentationService: TiltakDeltakerPresentationService,
	private val authService: AuthService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService
) {

	@Protected
	@GetMapping("/{tiltakDeltakerId}")
	fun hentTiltakDeltakerDetaljer(@PathVariable("tiltakDeltakerId") deltakerId: UUID): TiltakDeltakerDetaljerDto {
		val deltakerDetaljer = deltakerPresentationService.getDeltakerDetaljerById(deltakerId)

		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, deltakerDetaljer.gjennomforing.id)

		return deltakerDetaljer
	}

}
