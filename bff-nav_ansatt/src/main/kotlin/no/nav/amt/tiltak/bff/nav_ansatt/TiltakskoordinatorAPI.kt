package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.lib.models.deltaker.DeltakerStatus.Aarsak
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
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
    ): Map<UUID, DeltMedArrangorStatus> {
		authService.validerErM2MToken()

		return deltakerService.delMedArrangor(body).mapValues { (_, status) -> status.toDeltMedArrangorStatus()  }
	}
}

private fun DeltakerStatus.toDeltMedArrangorStatus(): DeltMedArrangorStatus {
	require(type == DeltakerStatus.Type.SOKT_INN && aktiv)

	return DeltMedArrangorStatus(
		id = id,
		type = type,
		gyldigFra = gyldigFra,
		gyldigTil = null,
		opprettet = opprettetDato,
		erManueltDeltMedArrangor = erManueltDeltMedArrangor,
	)
}

data class DeltMedArrangorStatus(
	val id: UUID,
	val type: DeltakerStatus.Type,
	val aarsak: Aarsak? = null,
	val gyldigFra: LocalDateTime,
	val gyldigTil: LocalDateTime?,
	val opprettet: LocalDateTime,
	val erManueltDeltMedArrangor: Boolean = false,
)
