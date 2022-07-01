package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.endringsmelding.HentAktivEndringsmeldingForDeltakereQuery
import no.nav.amt.tiltak.tiltak.dto.AktivEndringsmeldingDto
import no.nav.amt.tiltak.tiltak.dto.GjennomforingDto
import no.nav.amt.tiltak.tiltak.dto.TiltakDeltakerDto
import no.nav.amt.tiltak.tiltak.dto.toDto
import no.nav.amt.tiltak.tiltak.repositories.HentGjennomforingerFraArrangorerQuery
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping(value = ["/api/gjennomforing", "/api/tiltaksarrangor/gjennomforing"])
class TiltakarrangorGjennomforingController(
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService,
	private val authService: AuthService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val hentAktivEndringsmeldingForDeltakereQuery: HentAktivEndringsmeldingForDeltakereQuery,
	private val hentGjennomforingerFraArrangorerQuery: HentGjennomforingerFraArrangorerQuery
) {

	private val log = LoggerFactory.getLogger(javaClass)

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping
	fun hentGjennomforinger(): List<GjennomforingDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val gjennomforingIder = arrangorAnsattTilgangService
			.hentGjennomforingIder(ansattPersonligIdent)

		val koordinatorer = gjennomforingService.getKoordinatorerForGjennomforinger(gjennomforingIder)

		return gjennomforingService.getGjennomforinger(gjennomforingIder)
			.map {
				it.toDto(
					koordinatorer = koordinatorer[it.id] ?: emptyList()
				)
			}
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/tilgjengelig")
	fun hentTilgjengeligeGjennomforinger(): List<GjennomforingDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val virksomheterMedKoordinatorTilgang =
			arrangorAnsattTilgangService.hentVirksomhetsnummereMedKoordinatorRettighet(ansattPersonligIdent)

		val gjennomforingIder = hentGjennomforingerFraArrangorerQuery
			.query(virksomheterMedKoordinatorTilgang)

		val filtrerteGjennomforinger = gjennomforingProdFilter(gjennomforingIder)

		val koordinatorer = gjennomforingService.getKoordinatorerForGjennomforinger(gjennomforingIder)

		return gjennomforingService.getGjennomforinger(filtrerteGjennomforinger)
			.map { it.toDto(koordinatorer[it.id] ?: emptyList()) }
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/{gjennomforingId}/tilgang")
	fun opprettTilgangTilGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID) {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val virksomheterMedKoordinatorTilgang =
			arrangorAnsattTilgangService.hentVirksomhetsnummereMedKoordinatorRettighet(ansattPersonligIdent)

		val gjennomforingIder = hentGjennomforingerFraArrangorerQuery.query(virksomheterMedKoordinatorTilgang)

		if (!gjennomforingIder.contains(gjennomforingId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		arrangorAnsattTilgangService.opprettTilgang(ansattPersonligIdent, gjennomforingId)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{gjennomforingId}")
	fun hentGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID): GjennomforingDto {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, gjennomforingId)

		try {
			val koordinatorerForGjennomforing = gjennomforingService.getKoordinatorerForGjennomforing(gjennomforingId)
			return gjennomforingService.getGjennomforing(gjennomforingId).toDto(koordinatorerForGjennomforing)
		} catch (e: NoSuchElementException) {
			log.error("Fant ikke gjennomforing", e)
			throw NoSuchElementException("Fant ikke gjennomforing med id $gjennomforingId")
		}
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{gjennomforingId}/deltakere")
	fun hentDeltakere(@PathVariable("gjennomforingId") gjennomforingId: UUID): List<TiltakDeltakerDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, gjennomforingId)

		val deltakere = deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId)
			.filter { !it.erUtdatert}

		val aktiveEndringsmeldinger = hentAktivEndringsmeldingForDeltakereQuery.query(deltakere.map { it.id })

		return deltakere
			.map { d ->
				val endringsmelding = aktiveEndringsmeldinger.find { it.deltakerId == d.id }
				d.toDto(endringsmelding?.let { AktivEndringsmeldingDto(endringsmelding.startDato) })
			}
	}

	// Dette er et midlertidig filter som skal fjernes snart™
	private fun gjennomforingProdFilter(gjennomforingIder: List<UUID>): List<UUID> {
		val erIProd = System.getenv()["NAIS_CLUSTER_NAME"] == "prod-gcp"

		if (!erIProd)
			return gjennomforingIder

		val allowList = listOf(
			"18abdf60-0c2b-40b1-a552-ffc05868d373",
			"84c1d59e-6b6e-440d-897b-aa063ec43b04",
			"72e65187-f5db-4c28-b7dc-25866b7d5f2b",
			"ea82afc3-14f3-40ef-b80b-ffd07953ef37",
			"34dd5503-d01c-4577-afba-9b56ccbe7b33",
			"f236ffc0-9c28-4bad-ab31-3ffc67564199",
			"15c600c6-ed5f-4492-b7a2-e2706b66bc87",
			"4fd30e67-0ba6-4f19-80dd-8f539b1fb3a0",
		).map { UUID.fromString(it) }

		return gjennomforingIder.filter { allowList.contains(it) }
	}

}
