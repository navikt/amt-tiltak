package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.GjennomforingDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.KoordinatorDto
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.toDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle.KOORDINATOR
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
		arrangorAnsattTilgangService.shouldHaveRolle(ansattPersonligIdent, KOORDINATOR)

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
		arrangorAnsattTilgangService.shouldHaveRolle(ansattPersonligIdent, KOORDINATOR)

		val ansattId = arrangorAnsattTilgangService.hentAnsattId(ansattPersonligIdent)

		return hentGjennomforingerSomKanLeggesTil(ansattId).map { it.toDto() }
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/{gjennomforingId}/tilgang")
	fun opprettTilgangTilGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID) {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		arrangorAnsattTilgangService.shouldHaveRolle(ansattPersonligIdent, KOORDINATOR)

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
		arrangorAnsattTilgangService.shouldHaveRolle(ansattPersonligIdent, KOORDINATOR)

		arrangorAnsattTilgangService.fjernTilgang(ansattPersonligIdent, gjennomforingId)
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{gjennomforingId}")
	fun hentGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID): GjennomforingDto {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId).toDto()
		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, gjennomforingId, KOORDINATOR)
		return gjennomforing
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/{gjennomforingId}/koordinatorer")
	fun hentKoordinatorerPaGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID): List<KoordinatorDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, gjennomforingId, KOORDINATOR)

		return arrangorAnsattService.getKoordinatorerForGjennomforing(gjennomforingId)
			.map {
				KoordinatorDto(
					fornavn = it.fornavn,
					mellomnavn = it.mellomnavn,
					etternavn = it.etternavn,
				)
			}
	}

	private fun hentGjennomforingerSomKanLeggesTil(ansattId: UUID): List<Gjennomforing> {
		return arrangorAnsattTilgangService.hentAnsattTilganger(ansattId)
			.filter { it.roller.contains(KOORDINATOR) }
			.map { gjennomforingService.getByArrangorId(it.arrangorId) }
			.flatten()
			.filter(this::erSynligForArrangor)
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
