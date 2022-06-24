package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.tiltak.dto.GjennomforingDto
import no.nav.amt.tiltak.tiltak.dto.TiltakDeltakerDto
import no.nav.amt.tiltak.tiltak.dto.toDto
import no.nav.amt.tiltak.tiltak.repositories.HentTilgjengeligGjennomforingerQuery
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlin.NoSuchElementException

@RestController
@RequestMapping(value = [ "/api/gjennomforing", "/api/tiltaksarrangor/gjennomforing" ])
class TiltakarrangorGjennomforingController(
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService,
	private val authService: AuthService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val hentTilgjengeligGjennomforingerQuery: HentTilgjengeligGjennomforingerQuery
) {

	private val log = LoggerFactory.getLogger(javaClass)

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping
	fun hentGjennomforinger(): List<GjennomforingDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val gjennomforingIder = arrangorAnsattTilgangService
			.hentGjennomforingIder(ansattPersonligIdent)

		return gjennomforingService.getGjennomforinger(gjennomforingIder)
			.map { it.toDto() }
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/tilgjengelig")
	fun hentTilgjengeligeGjennomforinger(): List<GjennomforingDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val virksomheterMedKoordinatorTilgang =
			arrangorAnsattTilgangService.hentVirksomhetsnummereMedKoordinatorRettighet(ansattPersonligIdent)

		val gjennomforingIder = hentTilgjengeligGjennomforingerQuery.query(virksomheterMedKoordinatorTilgang)

		// TODO: Filter gjennomføringer med ID i prod

		return gjennomforingService.getGjennomforinger(gjennomforingIder)
			.map { it.toDto() }
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/{gjennomforingId}/tilgang")
	fun opprettTilgangTilGjennomforing(@PathVariable("gjennomforingId") gjennomforingId: UUID) {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		val virksomheterMedKoordinatorTilgang =
			arrangorAnsattTilgangService.hentVirksomhetsnummereMedKoordinatorRettighet(ansattPersonligIdent)

		val gjennomforingIder = hentTilgjengeligGjennomforingerQuery.query(virksomheterMedKoordinatorTilgang)

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
			return gjennomforingService.getGjennomforing(gjennomforingId).toDto()
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

		return deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId)
			.filter { !it.erUtdatert}
			.map { it.toDto() }
	}

}
