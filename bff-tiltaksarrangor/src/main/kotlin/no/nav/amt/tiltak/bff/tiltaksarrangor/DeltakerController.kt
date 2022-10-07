package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.DeltakerDetaljerDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.AuditLoggerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(value = [ "/api/tiltak-deltaker", "/api/tiltaksarrangor/tiltak-deltaker" ])
class DeltakerController(
	private val controllerService: ControllerService,
	private val authService: AuthService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val auditLoggerService: AuditLoggerService,
	private val arrangorAnsattService: ArrangorAnsattService
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{tiltakDeltakerId}")
	fun hentTiltakDeltakerDetaljer(@PathVariable("tiltakDeltakerId") deltakerId: UUID): DeltakerDetaljerDto {
		val deltakerDetaljer = controllerService.getDeltakerDetaljerById(deltakerId)
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		val ansatt = arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent)
			?: throw NoSuchElementException("Arrangor ansatt finnes ikke")

		auditLoggerService.tiltaksarrangorAnsattDeltakerOppslagAuditLog(ansatt.id, deltakerId)

		if (deltakerDetaljer.status.type == Deltaker.Status.PABEGYNT)
			throw UnauthorizedException("Har ikke tilgang til id $deltakerId")

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, deltakerDetaljer.gjennomforing.id)

		return deltakerDetaljer
	}

}
