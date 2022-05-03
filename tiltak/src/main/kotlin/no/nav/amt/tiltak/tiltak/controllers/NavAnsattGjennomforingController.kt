package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.amt.tiltak.tiltak.repositories.HentGjennomforingMedLopenrQuery
import no.nav.amt.tiltak.tiltak.repositories.HentTiltaksoversiktQuery
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/gjennomforing")
class NavAnsattGjennomforingController(
	private val authService: AuthService,
	private val gjennomforingService: GjennomforingService,
	private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService,
	private val hentGjennomforingMedLopenrQuery: HentGjennomforingMedLopenrQuery,
	private val hentTiltaksoversiktQuery: HentTiltaksoversiktQuery
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping
	fun hentTiltaksoversikt(): List<TiltaksoversiktGjennomforingDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		val tilganger = tiltaksansvarligTilgangService.hentAktiveTilganger(navIdent)
			.map { it.gjennomforingId }

		return hentTiltaksoversiktQuery.query(tilganger)
			.map {
				TiltaksoversiktGjennomforingDto(
					id = it.id,
					navn = it.navn,
					arrangorNavn = it.arrangorOrganisasjonsnavn ?: it.arrangorVirksomhetsnavn
				)
			}
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping("/{gjennomforingId}")
	fun hentGjennomforing(@PathVariable gjennomforingId: UUID): GjennomforingDto {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		if (!tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId)

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

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping(params = ["lopenr"])
	fun hentGjennomforingerMedLopenr(@RequestParam("lopenr") lopenr: Int): List<HentGjennomforingMedLopenrDto> {
		return hentGjennomforingMedLopenrQuery.query(lopenr)
			.map {
				HentGjennomforingMedLopenrDto(
					id = it.id,
					navn = it.navn,
					lopenr = it.lopenr,
					opprettetAr = it.opprettetAr,
					arrangorNavn = it.arrangorOrganisasjonsnavn ?: it.arrangorVirksomhetsnavn
				)
			}
	}

	data class TiltaksoversiktGjennomforingDto(
		val id: UUID,
		val navn: String,
		val arrangorNavn: String,
	)

	data class HentGjennomforingMedLopenrDto(
		val id: UUID,
		val navn: String,
		val lopenr: Int,
		val opprettetAr: Int,
		val arrangorNavn: String,
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
