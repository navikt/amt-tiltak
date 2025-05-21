package no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class TiltaksansvarligAutoriseringServiceImpl(
	private val authService: AuthService,
	private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService
) : TiltaksansvarligAutoriseringService {

	override fun verifiserTilgangTilFlate(navAnsattAzureId: UUID) {
		val harTilgang = authService.harTilgangTilTiltaksansvarligflate()

		if (!harTilgang) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Mangler tilgang til AD-gruppe")
		}
	}

	override fun verifiserTilgangTilEndringsmelding(navAnsattAzureId: UUID) {
		val harTilgang = authService.harTilgangTilEndringsmeldinger()

		if (!harTilgang) {
			throw UnauthorizedException("Mangler tilgang til AD-gruppe")
		}
	}

	override fun verifiserTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID) {
		val harTilgang = tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId)

		if (!harTilgang) {
			throw UnauthorizedException("Ikke tilgang til gjennomføring")
		}
	}
}
