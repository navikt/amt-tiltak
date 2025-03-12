package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(
	"/api/tiltakskoordinator"
)
class TiltakskoordinatorAPI(
	private val authService: AuthService,
	private val deltakerService: DeltakerService,
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PostMapping
	fun delMedArrangor(
		@RequestBody body: List<UUID>,
    ): Map<UUID, DeltakerStatus> {
		authService.validerErM2MToken()

		return deltakerService.delMedArrangor(body)
	}
}
