package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.GjennomforingDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.Person
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*

@RestController("GjennomforingControllerTiltaksarrangor")
@RequestMapping(value = ["/api/gjennomforing", "/api/tiltaksarrangor/gjennomforing"])
class GjennomforingController(
	private val gjennomforingService: GjennomforingService,
	private val authService: AuthService,
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
	fun hentKoordinatorerPaGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID): Set<Person> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, gjennomforingId)

		return gjennomforingService.getKoordinatorerForGjennomforing(gjennomforingId)
	}


	// Dette er et midlertidig filter som skal fjernes snart™
	private fun tillattIProd(gjennomforingId: UUID): Boolean {
		val erIProd = System.getenv()["NAIS_CLUSTER_NAME"] == "prod-gcp"

		if (!erIProd) return true

		val allowList = listOf(
			"18abdf60-0c2b-40b1-a552-ffc05868d373",
			"ea82afc3-14f3-40ef-b80b-ffd07953ef37",
			"f236ffc0-9c28-4bad-ab31-3ffc67564199",
			"84c1d59e-6b6e-440d-897b-aa063ec43b04",
			"b5f24f50-ff4b-42db-a7f5-0c6b4c994098",
			"94bc335a-293c-461f-91ce-6d3af3391f25",
			"4fd30e67-0ba6-4f19-80dd-8f539b1fb3a0",
			"34dd5503-d01c-4577-afba-9b56ccbe7b33",
			"a6cd35be-e29d-4be5-8c01-346f6822c829",
			"72e65187-f5db-4c28-b7dc-25866b7d5f2b",
			"15c600c6-ed5f-4492-b7a2-e2706b66bc87",
			"1b5fdc3e-356b-4fe3-a93b-384981810c12",
			"67077b36-869a-4328-9a8e-685d2bb42c14",
			"9fc77a48-8c8c-41ec-aae1-b7c1d0710030",
			"c8a5f4f9-d405-44ca-adc7-7acbb485666b",
			"ffb9f511-fa69-40ef-bcd8-32040127f6e1"

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
