package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.amt.tiltak.tiltak.repositories.AntallAktiveEndringsmeldingerQuery
import no.nav.amt.tiltak.tiltak.repositories.HentGjennomforingMedLopenrQuery
import no.nav.amt.tiltak.tiltak.repositories.HentTiltaksoversiktQuery
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/gjennomforing")
class NavAnsattGjennomforingController(
	private val authService: AuthService,
	private val gjennomforingService: GjennomforingService,
	private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService,
	private val hentGjennomforingMedLopenrQuery: HentGjennomforingMedLopenrQuery,
	private val hentTiltaksoversiktQuery: HentTiltaksoversiktQuery,
	private val tiltaksansvarligAutoriseringService: TiltaksansvarligAutoriseringService,
	private val antallAktiveEndringsmeldingerQuery: AntallAktiveEndringsmeldingerQuery
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping
	fun hentGjennomforinger(): List<HentGjennomforingerDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		tiltaksansvarligAutoriseringService.verifiserTilgangTilFlate(navAnsattAzureId)

		val tilganger = tiltaksansvarligTilgangService.hentAktiveTilganger(navIdent)
			.map { it.gjennomforingId }

		val antallEndringsmeldinger = antallAktiveEndringsmeldingerQuery.query(tilganger)

		return hentTiltaksoversiktQuery.query(tilganger)
			.map {
				HentGjennomforingerDto(
					id = it.id,
					navn = it.navn,
					lopenr = it.lopenr,
					opprettetAar = it.opprettetAar,
					arrangorNavn = it.arrangorOrganisasjonsnavn ?: it.arrangorVirksomhetsnavn,
					antallAktiveEndringsmeldinger =
						antallEndringsmeldinger.find { a -> a.gjennomforingId == it.id }?.antallMeldinger ?: 0
				)
			}
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping("/{gjennomforingId}")
	fun hentGjennomforing(@PathVariable gjennomforingId: UUID): GjennomforingDto {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		tiltaksansvarligAutoriseringService.verifiserTilgangTilFlate(navAnsattAzureId)
		tiltaksansvarligAutoriseringService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)

		val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId)

		return GjennomforingDto(
			id = gjennomforing.id,
			navn = gjennomforing.navn,
			startDato = gjennomforing.startDato,
			sluttDato = gjennomforing.sluttDato,
			tiltakNavn = gjennomforing.tiltak.navn,
			arrangor = ArrangorDto(
				virksomhetNavn = gjennomforing.arrangor.navn,
				organisasjonNavn = gjennomforing.arrangor.overordnetEnhetNavn
			),
			// I praksis så kan ikke disse være null, når vi får SAK i prod og alle gjennomføringer har løpenr/år så kan dette forenkles
			lopenr = gjennomforing.lopenr ?: 0,
			opprettetAr = gjennomforing.opprettetAar ?: 0
		)
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping(params = ["lopenr"])
	fun hentGjennomforingerMedLopenr(@RequestParam("lopenr") lopenr: Int): List<HentGjennomforingMedLopenrDto> {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()

		tiltaksansvarligAutoriseringService.verifiserTilgangTilFlate(navAnsattAzureId)

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

	data class HentGjennomforingerDto(
		val id: UUID,
		val navn: String,
		val arrangorNavn: String,
		val lopenr: Int,
		val opprettetAar: Int,
		val antallAktiveEndringsmeldinger: Int
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
		val tiltakNavn: String,
		val startDato: LocalDate?,
		val sluttDato: LocalDate?,
		val arrangor: ArrangorDto,
		val lopenr: Int,
		val opprettetAr: Int
	)

	data class ArrangorDto(
		val virksomhetNavn: String,
		val organisasjonNavn: String?
	)

}
