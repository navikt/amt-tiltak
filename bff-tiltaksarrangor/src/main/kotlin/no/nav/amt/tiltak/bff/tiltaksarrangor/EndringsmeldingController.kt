package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController("EndringsmeldingControllerTiltaksarrangor")
@RequestMapping("/api/tiltaksarrangor/endringsmelding")
class EndringsmeldingController(
	private val endringsmeldingService: EndringsmeldingService,
	private val arrangorTilgangService: ArrangorAnsattTilgangService,
	private val deltakerService: DeltakerService,
	private val authService: AuthService,
) {

	@GetMapping("/aktiv")
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun hentAktiveEndringsmeldinger(@RequestParam("deltakerId") deltakerId: UUID): List<EndringsmeldingDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		val erSkjermetDeltaker = deltakerService.erSkjermet(deltakerId)

		arrangorTilgangService.verifiserTilgangTilDeltaker(ansattPersonligIdent, deltakerId)

		if(erSkjermetDeltaker) return emptyList()

		return endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(deltakerId)
			.map {
				EndringsmeldingDto(
					id = it.id,
					innhold = it.innhold.toDto(),
				)
			}
	}
	@PatchMapping("/{id}/tilbakekall")
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun tilbakekallEndringsmelding(@PathVariable("id") id: UUID) {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val endringsmelding = endringsmeldingService.hentEndringsmelding(id)
		val erSkjermetDeltaker = deltakerService.erSkjermet(endringsmelding.deltakerId)

		arrangorTilgangService.verifiserTilgangTilDeltaker(ansattPersonligIdent, endringsmelding.deltakerId)

		if (erSkjermetDeltaker) throw UnauthorizedException("Kan ikke endre skjermet person")

		endringsmeldingService.markerSomTilbakekalt(endringsmelding.id)


	}
}
