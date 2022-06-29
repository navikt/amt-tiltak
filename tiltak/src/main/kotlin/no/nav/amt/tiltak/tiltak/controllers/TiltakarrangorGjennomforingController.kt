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
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(value = [ "/api/gjennomforing", "/api/tiltaksarrangor/gjennomforing" ])
class TiltakarrangorGjennomforingController(
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService,
	private val authService: AuthService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val hentAktivEndringsmeldingForDeltakereQuery: HentAktivEndringsmeldingForDeltakereQuery
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

		val deltakere = deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId)
			.filter { !it.erUtdatert}

		val aktiveEndringsmeldinger = hentAktivEndringsmeldingForDeltakereQuery.query(deltakere.map { it.id })

		return deltakere
			.map { d ->
				d.toDto(AktivEndringsmeldingDto(aktiveEndringsmeldinger.find { it.deltakerId == d.id }?.startDato))
			}
	}

}
