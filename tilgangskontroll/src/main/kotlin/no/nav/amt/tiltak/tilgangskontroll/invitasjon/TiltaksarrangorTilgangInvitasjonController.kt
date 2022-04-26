package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/tiltaksarrangor/tilgang/invitasjon")
class TiltaksarrangorTilgangInvitasjonController(
	private val tilgangInvitasjonService: TilgangInvitasjonService,
	private val authService: AuthService,
) {

	@GetMapping("/{invitasjonId}/info")
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun hentInvitasjonInfo(@PathVariable invitasjonId: UUID): InvitasjonInfoDto {
		return toDto(tilgangInvitasjonService.hentInvitasjonInfo(invitasjonId))
	}

	@PatchMapping("/{invitasjonId}/aksepter")
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun aksepterInvitasjon(@PathVariable invitasjonId: UUID) {
		val personligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		tilgangInvitasjonService.aksepterInvitasjon(invitasjonId, personligIdent)
	}

	private fun toDto(dbo: InvitasjonInfoDbo): InvitasjonInfoDto {
		return InvitasjonInfoDto(
			overordnetEnhetNavn = dbo.overordnetEnhetNavn,
				gjennomforingNavn = dbo.gjennomforingNavn,
				erBrukt = dbo.erBrukt,
				gyldigTil = dbo.gyldigTil,
		)
	}

	data class InvitasjonInfoDto(
		val overordnetEnhetNavn: String,
		val gjennomforingNavn: String,
		val erBrukt: Boolean,
		val gyldigTil: ZonedDateTime
	)

}
