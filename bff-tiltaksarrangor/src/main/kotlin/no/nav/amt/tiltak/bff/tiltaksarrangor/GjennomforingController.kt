package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.*
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle.KOORDINATOR
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.*
import no.nav.common.featuretoggle.UnleashClient
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*

@RestController("GjennomforingControllerTiltaksarrangor")
@RequestMapping("/api/tiltaksarrangor")
class GjennomforingController(
	private val gjennomforingService: GjennomforingService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val mineDeltakerlisterService: MineDeltakerlisterService,
	private val unleashClient: UnleashClient,
	private val controllerService: ControllerService
) {
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/gjennomforing/{gjennomforingId}/tilgang")
	fun opprettTilgangTilGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID) {
		val ansattId = controllerService.hentInnloggetAnsatt().id
		val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId)

		if (!erSynligForArrangor(gjennomforing) || (gjennomforing.erKurs && !kursTiltakToggleEnabled() && !erPilot(gjennomforingId))) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		arrangorAnsattTilgangService.verifiserRolleHosArrangor(ansattId, gjennomforing.arrangor.id, KOORDINATOR)

		mineDeltakerlisterService.leggTil(
			UUID.randomUUID(),
			ansattId,
			gjennomforingId
		)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@DeleteMapping("/gjennomforing/{gjennomforingId}/tilgang")
	fun fjernTilgangTilGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID) {
		val ansattId = controllerService.hentInnloggetAnsatt().id
		val arrangorId = gjennomforingService.getArrangorId(gjennomforingId)

		arrangorAnsattTilgangService.verifiserRolleHosArrangor(ansattId, arrangorId, KOORDINATOR)
		mineDeltakerlisterService.fjern(ansattId, gjennomforingId)
	}

	private fun erSynligForArrangor(gjennomforing: Gjennomforing): Boolean {
		if (gjennomforing.status in listOf(Gjennomforing.Status.GJENNOMFORES, Gjennomforing.Status.APENT_FOR_INNSOK)) return true
		else if (
			gjennomforing.status == Gjennomforing.Status.AVSLUTTET
			&& gjennomforing.sluttDato != null
			&& LocalDate.now().isBefore(gjennomforing.sluttDato!!.plusDays(15))
		// Gjennomforing er synlig til og med 14 dager etter avslutting
		) return true

		return false
	}

	private fun kursTiltakToggleEnabled(): Boolean {
		return unleashClient.isEnabled("amt.eksponer-kurs")
	}

	private fun erPilot(gjennomforingId: UUID): Boolean {
		return gjennomforingId.toString() in listOf("69afc1b8-50b9-472a-8b92-254dec821c3a", "e41ef5c5-2c2e-41f6-97a2-36fca4902b86", "67b63927-3c6f-494b-ad9b-5fff08b8d196")
	}
}
