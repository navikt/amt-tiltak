package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/tilgang/invitasjon")
class NavAnsattTilgangInvitasjonController(
	private val tilgangInvitasjonService: TilgangInvitasjonService,
	private val authService: AuthService,
	private val veilederService: VeilederService
) {

	@GetMapping("/ubrukt")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentUbrukteInvitasjoner(@RequestParam gjennomforingId: UUID): List<UbruktInvitasjonDto> {
		// Mangler tilgangskontroll på nav enhet

		return tilgangInvitasjonService.hentUbrukteInvitasjoner(gjennomforingId).map { toDto(it) }
	}

	@PostMapping
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun opprettInvitasjon(@RequestBody request: OpprettInvitasjonRequest) {
		// Mangler tilgangskontroll på nav enhet

		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = veilederService.getOrCreateVeileder(navIdent)

		tilgangInvitasjonService.opprettInvitasjon(request.gjennomforingId, navAnsatt.id)
	}

	@PatchMapping("/{invitasjonId}/avbryt")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun avbrytInvitasjon(@PathVariable invitasjonId: UUID) {
		// Mangler tilgangskontroll på nav enhet

		tilgangInvitasjonService.avbrytInvitasjon(invitasjonId)
	}

	private fun toDto(invitasjonDbo: TilgangInvitasjonDbo): UbruktInvitasjonDto {
		return UbruktInvitasjonDto(
			id = invitasjonDbo.id,
			opprettetAvNavIdent = invitasjonDbo.opprettetAvNavAnsattId.toString(),
			opprettetDato = invitasjonDbo.createdAt,
			gyldigTilDato = invitasjonDbo.gydligTil,
		)
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

}
