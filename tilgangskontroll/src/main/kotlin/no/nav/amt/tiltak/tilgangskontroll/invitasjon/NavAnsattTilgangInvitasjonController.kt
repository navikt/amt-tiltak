package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattTilgangService
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/tilgang/invitasjon")
class NavAnsattTilgangInvitasjonController(
	private val tilgangInvitasjonService: TilgangInvitasjonService,
	private val authService: AuthService,
	private val veilederService: VeilederService,
	private val navAnsattTilgangService: NavAnsattTilgangService
) {

	@GetMapping("/ubrukt")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentUbrukteInvitasjoner(@RequestParam gjennomforingId: UUID): List<UbruktInvitasjonDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		verifisierTilgangTilGjennomforing(navIdent, gjennomforingId)

		return tilgangInvitasjonService.hentUbrukteInvitasjoner(gjennomforingId).map { toDto(it) }
	}

	@PostMapping
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@ResponseStatus(HttpStatus.CREATED)
	fun opprettInvitasjon(@RequestBody request: OpprettInvitasjonRequest) {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = veilederService.getOrCreateVeileder(navIdent)

		verifisierTilgangTilGjennomforing(navIdent, request.gjennomforingId)

		tilgangInvitasjonService.opprettInvitasjon(request.gjennomforingId, navAnsatt.id)
	}

	@DeleteMapping("/{invitasjonId}")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun slettInvitasjon(@PathVariable invitasjonId: UUID) {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		val invitasjon = tilgangInvitasjonService.hentInvitasjon(invitasjonId)

		verifisierTilgangTilGjennomforing(navIdent, invitasjon.gjennomforingId)

		tilgangInvitasjonService.slettInvitasjon(invitasjonId)
	}

	data class UbruktInvitasjonDto(
		val id: UUID,
		val opprettetAvNavIdent: String,
		val opprettetDato: ZonedDateTime,
		val gyldigTilDato: ZonedDateTime,
	)

	data class OpprettInvitasjonRequest(
		val gjennomforingId: UUID
	)

	private fun verifisierTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID) {
		if (!navAnsattTilgangService.harTiltaksansvarligTilgangTilGjennomforing(navIdent, gjennomforingId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Har ikke tilgang til gjennomføring")
		}
	}

	private fun toDto(dbo: UbruktInvitasjonDbo): UbruktInvitasjonDto {
		return UbruktInvitasjonDto(
			id = dbo.id,
			opprettetAvNavIdent = dbo.opprettetAvNavIdent,
			opprettetDato = dbo.opprettetDato,
			gyldigTilDato = dbo.gyldigTilDato,
		)
	}

}
