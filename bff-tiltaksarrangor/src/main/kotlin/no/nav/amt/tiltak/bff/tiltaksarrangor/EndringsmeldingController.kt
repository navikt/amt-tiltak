package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.exceptions.SkjultDeltakerException
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
	private val controllerService: ControllerService
) {

	@GetMapping("/aktiv")
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun hentAktiveEndringsmeldinger(@RequestParam("deltakerId") deltakerId: UUID): List<EndringsmeldingDto> {
		val ansattId = controllerService.hentInnloggetAnsatt().id
		arrangorTilgangService.verifiserTilgangTilDeltaker(ansattId, deltakerId)

		if (deltakerService.erSkjultForTiltaksarrangor(deltakerId))
			throw SkjultDeltakerException("Deltaker med id $deltakerId er skjult for tiltaksarrangør")

		return endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(deltakerId)
			.map { it.toDto() }
	}

	@PatchMapping("/{id}/tilbakekall")
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun tilbakekallEndringsmelding(@PathVariable("id") id: UUID) {
		val ansattId = controllerService.hentInnloggetAnsatt().id
		val endringsmelding = endringsmeldingService.hentEndringsmelding(id)

		arrangorTilgangService.verifiserTilgangTilDeltaker(ansattId, endringsmelding.deltakerId)
		endringsmeldingService.markerSomTilbakekalt(endringsmelding.id)
	}

}
