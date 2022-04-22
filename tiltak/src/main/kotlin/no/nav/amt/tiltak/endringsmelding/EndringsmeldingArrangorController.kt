package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/tiltakarrangor/endringsmelding")
class EndringsmeldingArrangorController(
	private val endringsmeldingService: EndringsmeldingService,
	private val arrangorTilgangService: ArrangorAnsattTilgangService,
	private val deltakerService: DeltakerService,
	private val authService: AuthService
	) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/deltaker/{deltakerId}/startdato")
	fun registrerStartDato(@PathVariable("deltakerId") deltakerId: UUID, @RequestParam startDato: LocalDate) {

		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		val deltaker = deltakerService.hentDeltaker(deltakerId)
		val ansattId = arrangorTilgangService.hentAnsattId(ansattPersonligIdent)
		arrangorTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, deltaker.gjennomforingId)

		endringsmeldingService.opprettMedStartDato(deltakerId, startDato, ansattId)
	}
}
