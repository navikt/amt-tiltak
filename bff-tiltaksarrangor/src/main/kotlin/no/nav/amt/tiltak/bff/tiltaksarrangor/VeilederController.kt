package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.*
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.LeggTilVeiledereBulkRequest
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.LeggTilVeiledereRequest
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeilederInput
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tiltak.ArrangorVeiledersDeltaker
import no.nav.amt.tiltak.core.domain.tiltak.skjulesForAlleAktorer
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
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
	private val authService: AuthService,
	private val arrangorAnsattService: ArrangorAnsattService,
	private val arrangorVeilederService: ArrangorVeilederService,
	private val gjennomforingService: GjennomforingService,
	private val controllerService: ControllerService,
	private val deltakerService: DeltakerService,
	private val endringsmeldingService: EndringsmeldingService
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/veiledere")
	fun leggTilVeiledere(@RequestBody request: LeggTilVeiledereBulkRequest) {
		val ansatt = hentInnloggetAnsatt()
		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(
			ansatt.id,
			request.gjennomforingId,
		)

		val veiledere = request.veiledere.map { ArrangorVeilederInput(it.ansattId, it.erMedveileder) }
		verifiserVeilederTilganger(request.deltakerIder, veiledere.map { it.ansattId })

		arrangorVeilederService.opprettVeiledere(veiledere, request.deltakerIder)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/veiledere", params = ["deltakerId"])
	fun hentVeiledereForDeltaker(@RequestParam("deltakerId") deltakerId: UUID) : List<VeilederDto> {
		val ansatt = hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		val veiledere = arrangorVeilederService.hentVeiledereForDeltaker(deltakerId)

		return controllerService.mapAnsatteTilVeilederDtoer(veiledere)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/veiledere", params = ["deltakerId"])
	fun tildelVeiledereForDeltaker(
		@RequestParam("deltakerId") deltakerId: UUID,
		@RequestBody request: LeggTilVeiledereRequest,
	) {
		val ansatt = hentInnloggetAnsatt()
		val deltaker = deltakerService.hentDeltaker(deltakerId) ?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansatt.id, deltaker.gjennomforingId)

		val veiledere = request.veiledere.map { ArrangorVeilederInput(it.ansattId, it.erMedveileder) }

		verifiserVeilederTilganger(listOf(deltakerId), veiledere.map { it.ansattId })

		arrangorVeilederService.opprettVeiledereForDeltaker(veiledere, deltakerId)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/veiledere", params = ["gjennomforingId"])
	fun hentAktiveVeiledereForGjennomforing(@RequestParam("gjennomforingId") gjennomforingId: UUID) : List<VeilederDto> {
		val ansatt = hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(
			ansatt.id,
			gjennomforingId
		)

		val veiledere = arrangorVeilederService.hentAktiveVeiledereForGjennomforing(gjennomforingId)

		return controllerService.mapAnsatteTilVeilederDtoer(veiledere)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/veiledere/tilgjengelig")
	fun hentTilgjengeligeVeiledere(@RequestParam("gjennomforingId") gjennomforingId: UUID) : List<TilgjengeligVeilederDto> {
		val ansatt = hentInnloggetAnsatt()
		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(
			ansatt.id,
			gjennomforingId,
		)

		val tilgjengelige = arrangorVeilederService.hentTilgjengeligeVeiledereForGjennomforing(gjennomforingId)

		return tilgjengelige.map { TilgjengeligVeilederDto(it.id, it.fornavn, it.mellomnavn, it.etternavn) }
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/veileder/deltakerliste")
	fun hentDeltakerliste(): List<VeiledersDeltakerDto> {
		val ansatt = hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserHarRolleAnywhere(ansatt.id, ArrangorAnsattRolle.VEILEDER)

		val deltakerliste = arrangorVeilederService.hentDeltakerliste(ansatt.id).filter {
			arrangorAnsattTilgangService.harRolleHosArrangor(ansatt.id, it.arrangorId, ArrangorAnsattRolle.VEILEDER) &&
				!it.erUtdatert &&
				!it.status.skjulesForAlleAktorer()
		}

		val endringmeldingerMap =
			endringsmeldingService.hentAktiveEndringsmeldingerForDeltakere(deltakerliste.map { it.id })

		return deltakerliste.map {
			val endringsmeldinger = endringmeldingerMap.getOrDefault(it.id, emptyList())
				.map { e -> e.toDto() }
			it.toVeiledersDeltakerDto(endringsmeldinger)
		}
	}

	private fun hentInnloggetAnsatt(): Ansatt {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		return arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent)
			?: throw UnauthorizedException("Arrangor ansatt finnes ikke")
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

fun ArrangorVeiledersDeltaker.toVeiledersDeltakerDto(endringsmeldinger: List<EndringsmeldingDto>) =
	VeiledersDeltakerDto(
		id = id,
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
		fodselsnummer = fodselsnummer,
		startDato = startDato,
		sluttDato = sluttDato,
		status = DeltakerStatusDto(
			type = StatusTypeDto.valueOf(status.toString()),
			endretDato = statusDato
		),
		deltakerliste = DeltakerlisteDto(
			id = gjennomforingId,
			navn = gjennomforingNavn,
			type = gjennomforingType,
			startdato = null,
			sluttdato = null
		),
		erMedveilederFor = erMedveilederFor,
		aktiveEndringsmeldinger = endringsmeldinger
	)
