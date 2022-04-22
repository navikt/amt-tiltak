package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingerPaEnheterQuery
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/gjennomforing")
class NavAnsattGjennomforingController(
	private val authService: AuthService,
	private val navAnsattService: NavAnsattService,
	private val gjennomforingerPaEnheterQuery: GjennomforingerPaEnheterQuery,
	private val gjennomforingService: GjennomforingService
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping
	fun hentGjennomforinger(): List<HentAlleGjennomforingDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val enheter = navAnsattService.hentTiltaksansvarligEnhetTilganger(navIdent)

		return gjennomforingerPaEnheterQuery.query(enheter.map { it.kontor.id })
			.map { HentAlleGjennomforingDto(it.id, it.navn) }
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping("/{gjennomforingId}")
	fun hentGjennomforing(@PathVariable gjennomforingId: UUID): GjennomforingDto {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val enheter = navAnsattService.hentTiltaksansvarligEnhetTilganger(navIdent)

		val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId)

		if (!enheter.any { it.kontor.id == gjennomforing.navKontorId }) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til enhet")
		}

		return GjennomforingDto(
			id = gjennomforing.id,
			navn = gjennomforing.navn,
			startDato = gjennomforing.startDato,
			sluttDato = gjennomforing.sluttDato,
			arrangor = ArrangorDto(
				virksomhetNavn = gjennomforing.arrangor.navn,
				organisasjonNavn = gjennomforing.arrangor.overordnetEnhetNavn
			)
		)
	}

	data class HentAlleGjennomforingDto(
		val id: UUID,
		val navn: String,
	)

	data class GjennomforingDto(
		val id: UUID,
		val navn: String,
		val startDato: LocalDate?,
		val sluttDato: LocalDate?,
		val arrangor: ArrangorDto
	)

	data class ArrangorDto(
		val virksomhetNavn: String,
		val organisasjonNavn: String?
	)

}
