package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.DeltakerDetaljerDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.DeltakerDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.*
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.*
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(value = [ "/api/tiltak-deltaker", "/api/tiltaksarrangor/tiltak-deltaker" ])
class DeltakerController(
	private val controllerService: ControllerService,
	private val authService: AuthService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val auditLoggerService: AuditLoggerService,
	private val arrangorAnsattService: ArrangorAnsattService,
	private val deltakerService: DeltakerService,
	private val endringsmeldingService: EndringsmeldingService,
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping
	fun hentDeltakere(@RequestParam("gjennomforingId") gjennomforingId: UUID): List<DeltakerDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, gjennomforingId)

		val deltakere = deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId)
			.filter { it.status.type != Deltaker.Status.PABEGYNT }
			.filter { !it.erUtdatert }

		val endringmeldingerMap = endringsmeldingService.hentAktive(deltakere.map { it.id })

		return deltakere.map {
			val endringsmeldinger = endringmeldingerMap.getOrDefault(it.id, emptyList())
				.map { e -> EndringsmeldingDto(id = e.id, innhold = e.innhold.toDto()) }
			return@map it.toDto(endringsmeldinger)
		}
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{tiltakDeltakerId}")
	fun hentTiltakDeltakerDetaljer(@PathVariable("tiltakDeltakerId") deltakerId: UUID): DeltakerDetaljerDto {
		val deltakerDetaljer = controllerService.getDeltakerDetaljerById(deltakerId)
		val ansatt = hentInnloggetAnsatt()

		auditLoggerService.tiltaksarrangorAnsattDeltakerOppslagAuditLog(ansatt.id, deltakerId)

		if (deltakerDetaljer.status.type == Deltaker.Status.PABEGYNT)
			throw UnauthorizedException("Har ikke tilgang til id $deltakerId")

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		return deltakerDetaljer
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/{deltakerId}/oppstartsdato")
	fun leggTilOppstartsdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: LeggTilOppstartsdatoRequest,
	) {
		val ansatt = hentInnloggetAnsatt()
		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		kastHvisSkjermet(deltakerId)
		deltakerService.leggTilOppstartsdato(deltakerId, ansatt.id, request.oppstartsdato)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/oppstartsdato")
	fun endreOppstartsdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: EndreOppstartsdatoRequest,
	) {
		val ansatt = hentInnloggetAnsatt()
		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		kastHvisSkjermet(deltakerId)
		deltakerService.endreOppstartsdato(deltakerId, ansatt.id, request.oppstartsdato)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/avslutt-deltakelse")
	fun avsluttDeltakelse(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: AvsluttDeltakelseRequest,
	) {
		val ansatt = hentInnloggetAnsatt()
		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		kastHvisSkjermet(deltakerId)
		deltakerService.avsluttDeltakelse(deltakerId, ansatt.id, request.sluttdato, request.aarsak.toDeltakerStatusAarsak())
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/forleng-deltakelse")
	fun forlengDeltakelse(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: ForlengDeltakelseRequest,
	) {
		val ansatt = hentInnloggetAnsatt()
		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		kastHvisSkjermet(deltakerId)
		deltakerService.forlengDeltakelse(deltakerId, ansatt.id, request.sluttdato)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/ikke-aktuell")
	fun deltakerIkkeAktuell(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: DeltakerIkkeAktuellRequest,
	) {
		val ansatt = hentInnloggetAnsatt()
		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)
		kastHvisSkjermet(deltakerId)
		deltakerService.deltakerIkkeAktuell(deltakerId, ansatt.id, request.aarsak.toDeltakerStatusAarsak())
	}

	private fun hentInnloggetAnsatt(): Ansatt {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		return arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent)
			?: throw UnauthorizedException("Arrangor ansatt finnes ikke")
	}

	private fun kastHvisSkjermet (deltakerId: UUID) {
		val skjermetDeltaker = deltakerService.erSkjermet(deltakerId)

		if (skjermetDeltaker) throw UnauthorizedException("Kan ikke endre skjermet person")
	}

}
