package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController("EndringsmeldingAPITiltaksarrangor")
@RequestMapping("/api/tiltaksarrangor/endringsmelding")
class EndringsmeldingAPI(
	private val endringsmeldingService: EndringsmeldingService,
	private val arrangorTilgangService: ArrangorAnsattTilgangService,
	private val apiService: ApiService
) {
	@PatchMapping("/{id}/tilbakekall")
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun tilbakekallEndringsmelding(@PathVariable("id") id: UUID) {
		val ansattId = apiService.hentInnloggetAnsatt().id
		val endringsmelding = endringsmeldingService.hentEndringsmelding(id)

		arrangorTilgangService.verifiserTilgangTilDeltaker(ansattId, endringsmelding.deltakerId)
		endringsmeldingService.markerSomTilbakekalt(endringsmelding.id)
	}
}
