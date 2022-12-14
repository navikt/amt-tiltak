package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController("GjennomforingControllerNavAnsatt")
@RequestMapping("/api/nav-ansatt/gjennomforing")
class GjennomforingController(
	private val authService: AuthService,
	private val gjennomforingService: GjennomforingService,
	private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService,
	private val endringsmeldingService: EndringsmeldingService,
	private val tiltaksansvarligAutoriseringService: TiltaksansvarligAutoriseringService,
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping
	fun hentGjennomforinger(): List<HentGjennomforingerDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		tiltaksansvarligAutoriseringService.verifiserTilgangTilFlate(navAnsattAzureId)

		val tilganger = tiltaksansvarligTilgangService.hentAktiveTilganger(navIdent)
			.map { it.gjennomforingId }

		return gjennomforingService.getGjennomforinger(tilganger).map { gjennomforing ->
			val antallAktiveEndringsmeldinger = endringsmeldingService.hentAntallAktiveForGjennomforing(gjennomforing.id)
			val arrangor = gjennomforing.arrangor
			return@map HentGjennomforingerDto(
				id = gjennomforing.id,
				navn = gjennomforing.navn,
				lopenr = gjennomforing.lopenr,
				opprettetAar = gjennomforing.opprettetAar,
				arrangorNavn = arrangor.overordnetEnhetNavn ?: arrangor.navn,
				antallAktiveEndringsmeldinger = antallAktiveEndringsmeldinger,
				tiltak = TiltakDto(
					kode = gjennomforing.tiltak.kode,
					navn = gjennomforing.tiltak.navn,
				),
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

		val arrangor = gjennomforing.arrangor

		return GjennomforingDto(
			id = gjennomforing.id,
			navn = gjennomforing.navn,
			startDato = gjennomforing.startDato,
			sluttDato = gjennomforing.sluttDato,
			tiltakNavn = gjennomforing.tiltak.navn,
			arrangor = ArrangorDto(
				virksomhetNavn = arrangor.navn,
				virksomhetOrgnr = arrangor.organisasjonsnummer,
				organisasjonNavn = arrangor.overordnetEnhetNavn,
				organisasjonOrgnr = arrangor.overordnetEnhetOrganisasjonsnummer,
			),
			lopenr = gjennomforing.lopenr,
			opprettetAr = gjennomforing.opprettetAar,
			tiltak = TiltakDto(
				kode = gjennomforing.tiltak.kode,
				navn = gjennomforing.tiltak.navn,
			),
		)
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping(params = ["lopenr"])
	fun hentGjennomforingerMedLopenr(@RequestParam("lopenr") lopenr: Int): List<HentGjennomforingMedLopenrDto> {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()

		tiltaksansvarligAutoriseringService.verifiserTilgangTilFlate(navAnsattAzureId)

		return gjennomforingService.getAktiveByLopenr(lopenr).map {
			HentGjennomforingMedLopenrDto(
				id = it.id,
				navn = it.navn,
				lopenr = it.lopenr,
				opprettetAr = it.opprettetAar,
				arrangorNavn = it.arrangor.overordnetEnhetNavn ?: it.arrangor.navn,
				tiltak = TiltakDto(
					kode = it.tiltak.kode,
					navn = it.tiltak.navn,
				),
			)
		}
	}

	data class HentGjennomforingerDto(
		val id: UUID,
		val navn: String,
		val arrangorNavn: String,
		val lopenr: Int,
		val opprettetAar: Int,
		val antallAktiveEndringsmeldinger: Int,
		val tiltak: TiltakDto,
	)

	data class HentGjennomforingMedLopenrDto(
		val id: UUID,
		val navn: String,
		val lopenr: Int,
		val opprettetAr: Int,
		val arrangorNavn: String,
		val tiltak: TiltakDto,
	)

	data class GjennomforingDto(
		val id: UUID,
		val navn: String,
		val tiltakNavn: String,
		val startDato: LocalDate?,
		val sluttDato: LocalDate?,
		val arrangor: ArrangorDto,
		val lopenr: Int,
		val opprettetAr: Int,
		val tiltak: TiltakDto,
	)

	data class ArrangorDto(
		val virksomhetNavn: String,
		val virksomhetOrgnr: String,
		val organisasjonNavn: String?,
		val organisasjonOrgnr: String?
	)

	data class TiltakDto(
		val kode: String,
		val navn: String,
	)

}
