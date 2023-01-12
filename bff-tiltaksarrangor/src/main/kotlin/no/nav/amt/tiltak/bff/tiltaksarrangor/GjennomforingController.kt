package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.GjennomforingDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.KoordinatorDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*

@RestController("GjennomforingControllerTiltaksarrangor")
@RequestMapping("/api/tiltaksarrangor/gjennomforing")
class GjennomforingController(
	private val gjennomforingService: GjennomforingService,
	private val authService: AuthService,
	private val arrangorAnsattService: ArrangorAnsattService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping
	fun hentGjennomforinger(): List<GjennomforingDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val gjennomforingIder = arrangorAnsattTilgangService
			.hentGjennomforingIder(ansattPersonligIdent)

		return gjennomforingService.getGjennomforinger(gjennomforingIder)
			.filter(this::erSynligForArrangor)
			.map { it.toDto() }
	}


	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/tilgjengelig")
	fun hentTilgjengeligeGjennomforinger(): List<GjennomforingDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val ansattId = arrangorAnsattTilgangService.hentAnsattId(ansattPersonligIdent)

		return hentGjennomforingerSomKanLeggesTil(ansattId).map { it.toDto() }
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/{gjennomforingId}/tilgang")
	fun opprettTilgangTilGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID) {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val ansattId = arrangorAnsattTilgangService.hentAnsattId(ansattPersonligIdent)

		val gjennomforinger = hentGjennomforingerSomKanLeggesTil(ansattId)

		if (!gjennomforinger.map { it.id }.contains(gjennomforingId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		arrangorAnsattTilgangService.opprettTilgang(ansattPersonligIdent, gjennomforingId)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@DeleteMapping("/{gjennomforingId}/tilgang")
	fun fjernTilgangTilGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID) {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.fjernTilgang(ansattPersonligIdent, gjennomforingId)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{gjennomforingId}")
	fun hentGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID): GjennomforingDto {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId).toDto()
		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, gjennomforingId)
		return gjennomforing
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{gjennomforingId}/koordinatorer")
	fun hentKoordinatorerPaGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID): List<KoordinatorDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, gjennomforingId)

		return arrangorAnsattService.getKoordinatorerForGjennomforing(gjennomforingId)
			.map {
				KoordinatorDto(
					fornavn = it.fornavn,
					mellomnavn = it.mellomnavn,
					etternavn = it.etternavn,
				)
			}
	}


	// Dette er et midlertidig filter som skal fjernes snart™
	private fun tillattIProd(gjennomforingId: UUID): Boolean {
		val erIProd = System.getenv()["NAIS_CLUSTER_NAME"] == "prod-gcp"

		if (!erIProd) return true

		val allowList = listOf(
			"91d00de7-92a5-45b5-9537-f58a4a710567",
			"48ea7a78-ce87-4af1-b445-49475d08a8a3",
			"57dc526b-a591-4dbf-9df5-44ffff976744",
			"9c0799b1-9090-4b13-a7b4-cdb25c22cee1",
			"0c7a041b-38b8-4553-b025-c41f2a2f7a68",
			"693414d8-6d1e-4af4-89ae-ec5468fa41b5",
			"b1eff0ed-eb66-4103-8ccf-44f2939a805c",
			"5a08d97c-f826-4675-8702-752831ca0518",
			"7b8cc6e2-ad4d-41c1-8fd2-9d93813a0ff4",
			"64893ab6-5481-45cc-8471-fb2f95798598",
			"c179e87e-c574-4232-acc7-b9fbbbacfe75",
			"a3920e08-bf43-4037-9ad4-6f8749bc3bfb",
			"818c9a59-e0c2-4077-a64b-0f97b19e4149",
			"ddf754a6-d0bb-4827-bc83-61912ed32efa",
			"08df59a4-328c-4348-b0a8-173b2326d918",
			"4f755dfe-70b0-4ca1-bbcb-b3f38def709f",
			"005c35eb-aeb8-4cda-ae51-5a1623e3a06b",
			"0c8e9eb2-eb85-41b6-a921-91dcbdd7464e",
			"a7f325e1-baef-4fe0-b4ac-41dde04be997",
			"ce0cf1a3-9019-4ab6-907b-e20ef7a5d31e",
			"b0859b76-58f0-4ed3-b5bc-3c1e20e397f2",
		).map { UUID.fromString(it) }

		return allowList.contains(gjennomforingId)
	}

	private fun hentGjennomforingerSomKanLeggesTil(ansattId: UUID): List<Gjennomforing> {
		return arrangorAnsattTilgangService.hentAnsattTilganger(ansattId)
			.filter { it.roller.contains(ArrangorAnsattRolle.KOORDINATOR) }
			.map { gjennomforingService.getByArrangorId(it.arrangorId) }
			.flatten()
			.filter(this::erSynligForArrangor)
			.filter { tillattIProd(it.id) }
	}

	private fun erSynligForArrangor(gjennomforing: Gjennomforing): Boolean {
		if (gjennomforing.status == Gjennomforing.Status.GJENNOMFORES) return true
		else if (
			gjennomforing.status == Gjennomforing.Status.AVSLUTTET
			&& gjennomforing.sluttDato != null
			&& LocalDate.now().isBefore(gjennomforing.sluttDato!!.plusDays(15))
		// Gjennomforing er synlig til og med 14 dager etter avslutting
		) return true

		return false
	}
}
