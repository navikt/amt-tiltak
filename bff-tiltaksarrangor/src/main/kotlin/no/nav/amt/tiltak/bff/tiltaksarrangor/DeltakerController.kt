package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.DeltakerDetaljerDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.DeltakerDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.*
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tiltak.skjulesForAlleAktorer
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
	private val arrangorVeilederService: ArrangorVeilederService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val auditLoggerService: AuditLoggerService,
	private val deltakerService: DeltakerService,
	private val endringsmeldingService: EndringsmeldingService,
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping
	fun hentDeltakere(@RequestParam("gjennomforingId") gjennomforingId: UUID): List<DeltakerDto> {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansatt.id, gjennomforingId)

		var deltakere = deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId)
			.filter { !it.status.type.skjulesForAlleAktorer() }
			.filter { !it.erUtdatert }

		val erSkjultMap = deltakerService.erSkjultForTiltaksarrangor(deltakere.map { it.id })

		deltakere = deltakere.filter {
			!erSkjultMap.getOrDefault(it.id, false)
		}

		val endringmeldingerMap =
			endringsmeldingService.hentAktiveEndringsmeldingerForDeltakere(deltakere.map { it.id })

		val veiledere = arrangorVeilederService.hentAktiveVeiledereForGjennomforing(gjennomforingId)
		val veilederDtoer = controllerService.mapAnsatteTilVeilederDtoer(veiledere).groupBy { it.deltakerId }


		return deltakere.map {
			val endringsmeldinger = endringmeldingerMap.getOrDefault(it.id, emptyList())
				.map { e -> e.toDto() }

			val veiledereForDeltaker = veilederDtoer.getOrDefault(it.id, emptyList())

			return@map it.toDto(endringsmeldinger, veiledereForDeltaker)
		}
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{tiltakDeltakerId}")
	fun hentTiltakDeltakerDetaljer(@PathVariable("tiltakDeltakerId") deltakerId: UUID): DeltakerDetaljerDto {
		val ansatt = controllerService.hentInnloggetAnsatt()
		auditLoggerService.tiltaksarrangorAnsattDeltakerOppslagAuditLog(ansatt.id, deltakerId)
		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		val deltakerDetaljer = controllerService.getDeltakerDetaljerById(deltakerId)

		verifiserErIkkeSkjult(deltakerId)

		return deltakerDetaljer
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/{deltakerId}/oppstartsdato")
	fun leggTilOppstartsdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: LeggTilOppstartsdatoRequest,
	) {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		endringsmeldingService.opprettLeggTilOppstartsdatoEndringsmelding(deltakerId, ansatt.id, request.oppstartsdato)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/oppstartsdato")
	fun endreOppstartsdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: EndreOppstartsdatoRequest,
	) {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		endringsmeldingService.opprettEndreOppstartsdatoEndringsmelding(deltakerId, ansatt.id, request.oppstartsdato)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/avslutt-deltakelse")
	fun avsluttDeltakelse(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: AvsluttDeltakelseRequest,
	) {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		endringsmeldingService.opprettAvsluttDeltakelseEndringsmelding(deltakerId, ansatt.id, request.sluttdato, request.aarsak.toModel())
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/forleng-deltakelse")
	fun forlengDeltakelse(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: ForlengDeltakelseRequest,
	) {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		endringsmeldingService.opprettForlengDeltakelseEndringsmelding(deltakerId, ansatt.id, request.sluttdato)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/deltakelse-prosent")
	fun endreDeltakelsesprosent(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody body: EndreDeltakelsesprosentRequestBody
	) {
		if (body.deltakelseProsent <= 0) throw ValidationException("Deltakelsesprosent kan ikke være mindre eller lik 0")
		if (body.deltakelseProsent > 100) throw ValidationException("Deltakelsesprosent kan ikke være over 100%")

		body.dagerPerUke?.let {
			if (it < 1 || it > 5) throw ValidationException("Antall dager i uken må være mellom 1 og 5")
		}

		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		endringsmeldingService.opprettEndreDeltakelseProsentEndringsmelding(
			deltakerId = deltakerId,
			arrangorAnsattId = ansatt.id,
			deltakerProsent = body.deltakelseProsent,
			dagerPerUke = body.dagerPerUke,
			gyldigFraDato = body.gyldigFraDato
		)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/ikke-aktuell")
	fun deltakerIkkeAktuell(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: DeltakerIkkeAktuellRequest,
	) {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		endringsmeldingService.opprettDeltakerIkkeAktuellEndringsmelding(deltakerId, ansatt.id, request.aarsak.toModel())
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/er-aktuell")
	fun deltakerErAktuell(
		@PathVariable("deltakerId") deltakerId: UUID,
	) {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		endringsmeldingService.opprettErAktuellEndringsmelding(deltakerId, ansatt.id)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/endre-sluttdato")
	fun endreSluttdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: EndreSluttdatoRequest,
	) {
		val ansatt = controllerService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		verifiserErIkkeSkjult(deltakerId)

		endringsmeldingService.opprettEndresluttdatoEndringsmelding(deltakerId, ansatt.id, request.sluttdato)
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
	fun hentBrukerInfo(@PathVariable("deltakerId") deltakerId: UUID): Map<String, UUID?> {
		return deltakerService.hentBrukerInfo(deltakerId)
	}

	private fun verifiserErIkkeSkjult(deltakerId: UUID) {
		if (deltakerService.erSkjultForTiltaksarrangor(deltakerId))
			throw SkjultDeltakerException("Deltaker med id $deltakerId er skjult for tiltaksarrangør")
	}

	data class EndreDeltakelsesprosentRequestBody(
		val deltakelseProsent: Int,
		val dagerPerUke: Int?,
		val gyldigFraDato: LocalDate?
	)
}
