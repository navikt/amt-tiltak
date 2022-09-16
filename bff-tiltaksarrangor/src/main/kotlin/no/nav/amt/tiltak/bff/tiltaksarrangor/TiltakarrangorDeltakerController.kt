package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.TiltakDeltakerDetaljerDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(value = [ "/api/tiltak-deltaker", "/api/tiltaksarrangor/tiltak-deltaker" ])
class TiltakarrangorDeltakerController(
	private val deltakerPresentationService: TiltakDeltakerPresentationService,
	private val authService: AuthService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{tiltakDeltakerId}")
	fun hentTiltakDeltakerDetaljer(@PathVariable("tiltakDeltakerId") deltakerId: UUID): TiltakDeltakerDetaljerDto {
		val deltakerDetaljer = deltakerPresentationService.getDeltakerDetaljerById(deltakerId)

		if(deltakerDetaljer.status.type == Deltaker.Status.PABEGYNT)
			throw UnauthorizedException("Har ikke tilgang til id $deltakerId")

		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, deltakerDetaljer.gjennomforing.id)

		return deltakerDetaljer
	}

}
