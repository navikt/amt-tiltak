package no.nav.amt.tiltak.bff.tiltaksarrangor

import java.time.LocalDate
import java.util.UUID
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.AvsluttDeltakelseRequest
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.DeltakerIkkeAktuellRequest
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.EndreOppstartsdatoRequest
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.EndreSluttaarsakRequest
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.EndreSluttdatoRequest
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.ForlengDeltakelseRequest
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.LeggTilOppstartsdatoRequest
import no.nav.amt.tiltak.bff.tiltaksarrangor.request.RegistrerVurderingRequest
import no.nav.amt.tiltak.bff.tiltaksarrangor.response.OpprettEndringsmeldingResponse
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tiltaksarrangor/deltaker")
class DeltakerAPI(
	private val apiService: ApiService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val deltakerService: DeltakerService,
	private val endringsmeldingService: EndringsmeldingService,
) {
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/{deltakerId}/oppstartsdato")
	fun leggTilOppstartsdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: LeggTilOppstartsdatoRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = apiService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettLeggTilOppstartsdatoEndringsmelding(deltakerId, ansatt.id, request.oppstartsdato))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/oppstartsdato")
	fun endreOppstartsdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: EndreOppstartsdatoRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = apiService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettEndreOppstartsdatoEndringsmelding(deltakerId, ansatt.id, request.oppstartsdato))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/avslutt-deltakelse")
	fun avsluttDeltakelse(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: AvsluttDeltakelseRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = apiService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettAvsluttDeltakelseEndringsmelding(deltakerId, ansatt.id, request.sluttdato, request.aarsak.toModel()))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/forleng-deltakelse")
	fun forlengDeltakelse(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: ForlengDeltakelseRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = apiService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

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

		val ansatt = apiService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

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
		val ansatt = apiService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettDeltakerIkkeAktuellEndringsmelding(deltakerId, ansatt.id, request.aarsak.toModel()))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/endre-sluttdato")
	fun endreSluttdato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: EndreSluttdatoRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = apiService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettEndresluttdatoEndringsmelding(deltakerId, ansatt.id, request.sluttdato))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PatchMapping("/{deltakerId}/sluttaarsak")
	fun endreSluttaarsak(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: EndreSluttaarsakRequest,
	): OpprettEndringsmeldingResponse {
		val ansatt = apiService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		val deltaker = deltakerService.hentDeltaker(deltakerId)
		if (deltaker?.status?.type != DeltakerStatus.Type.HAR_SLUTTET) {
			throw ValidationException("Kan ikke opprette endreSluttaarsakEndringsmelding for deltaker med status ${deltaker?.status?.type}")
		}

		return OpprettEndringsmeldingResponse(endringsmeldingService.opprettEndreSluttaarsakEndringsmelding(deltakerId, ansatt.id, request.aarsak.toModel()))
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/{deltakerId}/vurdering")
	fun registrerVurdering(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestBody request: RegistrerVurderingRequest
	): List<Vurdering> {
		val ansatt = apiService.hentInnloggetAnsatt()

		arrangorAnsattTilgangService.verifiserTilgangTilDeltaker(ansatt.id, deltakerId)

		return deltakerService.lagreVurdering(
			Vurdering(
				id = request.id,
				deltakerId = deltakerId,
				opprettet = request.opprettet,
				opprettetAvArrangorAnsattId = ansatt.id,
				vurderingstype = request.vurderingstype,
				begrunnelse = request.begrunnelse
			)
		)
	}

	data class EndreDeltakelsesprosentRequestBody(
		val deltakelseProsent: Int,
		val dagerPerUke: Int?,
		val gyldigFraDato: LocalDate?
	)
}
