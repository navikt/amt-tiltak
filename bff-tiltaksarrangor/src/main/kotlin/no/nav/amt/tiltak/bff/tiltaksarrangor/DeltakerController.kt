package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.request.*
import no.nav.amt.tiltak.bff.tiltaksarrangor.response.OpprettEndringsmeldingResponse
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.exceptions.SkjultDeltakerException
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.amt.tiltak.core.port.*
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/tiltaksarrangor/deltaker")
class DeltakerController(
	private val controllerService: ControllerService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val deltakerService: DeltakerService,
	private val endringsmeldingService: EndringsmeldingService,
	private val authService: AuthService
) {
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/{deltakerId}/oppstartsdato")
	fun leggTilOppstartsdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: LeggTilOppstartsdatoRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettLeggTilOppstartsdatoEndringsmelding(deltakerId, ansatt.id, request.oppstartsdato))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/oppstartsdato")
	fun endreOppstartsdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: EndreOppstartsdatoRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettEndreOppstartsdatoEndringsmelding(deltakerId, ansatt.id, request.oppstartsdato))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/avslutt-deltakelse")
	fun avsluttDeltakelse(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: AvsluttDeltakelseRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettAvsluttDeltakelseEndringsmelding(deltakerId, ansatt.id, request.sluttdato, request.aarsak.toModel()))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/forleng-deltakelse")
	fun forlengDeltakelse(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: ForlengDeltakelseRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettForlengDeltakelseEndringsmelding(deltakerId, ansatt.id, request.sluttdato))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/deltakelse-prosent")
	fun endreDeltakelsesprosent(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody body: EndreDeltakelsesprosentRequestBody
	): OpprettEndringsmeldingResponse {
		if (body.deltakelseProsent <= 0) throw ValidationException("Deltakelsesprosent kan ikke være mindre eller lik 0")
		if (body.deltakelseProsent > 100) throw ValidationException("Deltakelsesprosent kan ikke være over 100%")

		body.dagerPerUke?.let {
			if (it < 1 || it > 5) throw ValidationException("Antall dager i uken må være mellom 1 og 5")
		}

		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettEndreDeltakelseProsentEndringsmelding(
			deltakerId = deltakerId,
			arrangorAnsattId = ansatt.id,
			deltakerProsent = body.deltakelseProsent,
			dagerPerUke = body.dagerPerUke,
			gyldigFraDato = body.gyldigFraDato
		))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/ikke-aktuell")
	fun deltakerIkkeAktuell(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: DeltakerIkkeAktuellRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettDeltakerIkkeAktuellEndringsmelding(deltakerId, ansatt.id, request.aarsak.toModel()))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/er-aktuell")
	fun deltakerErAktuell(
		@PathVariable("deltakerId") deltakerId: UUID,
	): OpprettEndringsmeldingResponse {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettErAktuellEndringsmelding(deltakerId, ansatt.id))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/endre-sluttdato")
	fun endreSluttdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: EndreSluttdatoRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettEndresluttdatoEndringsmelding(deltakerId, ansatt.id, request.sluttdato))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/skjul")
	fun skjulDeltakerForTiltaksarrangor(@PathVariable("deltakerId") deltakerId: UUID) {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		deltakerService.skjulDeltakerForTiltaksarrangor(deltakerId, ansatt.id)
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping("/{deltakerId}/bruker-info")
	fun hentBrukerInfo(@PathVariable("deltakerId") deltakerId: UUID): BrukerInfo {
		authService.validerErM2MToken()

		val bruker = deltakerService.hentBruker(deltakerId)
		return BrukerInfo(
			bruker.id,
			bruker.personIdentType?.name,
			bruker.historiskeIdenter,
			bruker.navEnhetId,
		)
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PostMapping("/bruker-info")
	fun hentBrukerInfoForPersonident(@RequestBody request: HentBrukerInfoRequest): BrukerInfo {
		authService.validerErM2MToken()

		val bruker = deltakerService.hentBruker(request.personident)
		return bruker?.let {
			BrukerInfo(
				brukerId = it.id,
				personIdentType = it.personIdentType?.name,
				historiskeIdenter = it.historiskeIdenter,
				navEnhetId = it.navEnhetId
			)
		} ?: throw NoSuchElementException("Fant ikke forespurt bruker")
	}

	private fun verifiserErIkkeSkjult(deltakerId: UUID) {
		if (deltakerService.erSkjultForTiltaksarrangor(deltakerId))
			throw SkjultDeltakerException("Deltaker med id $deltakerId er skjult for tiltaksarrangør")
	}

	data class EndreDeltakelsesprosentRequestBody(
		val deltakelseProsent: Int,
		val dagerPerUke: Float?,
		val gyldigFraDato: LocalDate?
	)
}


data class BrukerInfo(
	val brukerId: UUID,
	val personIdentType: String?,
	val historiskeIdenter: List<String>,
	val navEnhetId: UUID?,
)
