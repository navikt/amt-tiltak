package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.*
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.LeggTilVeiledereRequest
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.amt.tiltak.core.port.*
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController("VeilederControllerTiltaksarrangor")
@RequestMapping("/api/tiltaksarrangor")
class VeilederController (
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val arrangorVeilederService: ArrangorVeilederService,
	private val gjennomforingService: GjennomforingService,
	private val controllerService: ControllerService,
	private val deltakerService: DeltakerService
) {
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/veiledere", params = ["deltakerId"])
	fun tildelVeiledereForDeltaker(
		@RequestParam("deltakerId") deltakerId: UUID,
		@RequestBody request: LeggTilVeiledereRequest,
	) {
		val ansatt = controllerService.hentInnloggetAnsatt()
		val deltaker = deltakerService.hentDeltaker(deltakerId) ?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansatt.id, deltaker.gjennomforingId)

		val veiledere = request.veiledere.map { ArrangorVeilederInput(it.ansattId, it.erMedveileder) }

		verifiserVeilederTilganger(listOf(deltakerId), veiledere.map { it.ansattId })

		arrangorVeilederService.opprettVeiledereForDeltaker(veiledere, deltakerId)
	}

	private fun verifiserVeilederTilganger(deltakerIder: List<UUID>, veilederIder: List<UUID>) {
		val gjennomforingIder = deltakerService.hentDeltakere(deltakerIder)
			.map { it.gjennomforingId }.distinct()

		if (gjennomforingIder.size > 1) {
			throw ValidationException("Alle deltakere må være på samme gjennomføring for å tildele veiledere")
		}

		val arrangorId = gjennomforingService.getArrangorId(gjennomforingIder.first())

		veilederIder.forEach {
			val harVeilederTilgang = arrangorAnsattTilgangService.harRolleHosArrangor(it, arrangorId, ArrangorAnsattRolle.VEILEDER)
			if(!harVeilederTilgang) {
				throw ResponseStatusException(HttpStatus.FORBIDDEN, "Kan ikke tildele veileder som ikke har veiledertilgang")
			}
		}
	}
}
