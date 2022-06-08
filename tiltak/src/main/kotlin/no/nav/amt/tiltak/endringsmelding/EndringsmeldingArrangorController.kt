package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.SkjermetPersonService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/tiltaksarrangor/endringsmelding")
class EndringsmeldingArrangorController(
	private val skjermetPersonService: SkjermetPersonService,
	private val endringsmeldingService: EndringsmeldingService,
	private val arrangorTilgangService: ArrangorAnsattTilgangService,
	private val deltakerService: DeltakerService,
	private val authService: AuthService,
) {

	@GetMapping
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun hentEndringsmeldinger(@RequestParam("deltakerId") deltakerId: UUID): List<EndringsmeldingDto> {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorTilgangService.verifiserTilgangTilDeltaker(ansattPersonligIdent, deltakerId)

		return endringsmeldingService.hentEndringsmeldingerForDeltaker(deltakerId)
			.map {
				EndringsmeldingDto(
					id = it.id,
					startDato = it.startDato,
					aktiv = it.aktiv
				)
			}
	}

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@PostMapping("/deltaker/{deltakerId}/startdato")
	fun registrerStartDato(
		@PathVariable("deltakerId") deltakerId: UUID,
		@RequestParam("startDato") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDato: LocalDate
	) {
		val ansattPersonligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		val deltaker = deltakerService.hentDeltaker(deltakerId)
		val ansattId = arrangorTilgangService.hentAnsattId(ansattPersonligIdent)

		arrangorTilgangService.verifiserTilgangTilGjennomforing(ansattPersonligIdent, deltaker.gjennomforingId)

		val erSkjermet = skjermetPersonService.erSkjermet(deltaker.bruker!!.fodselsnummer)

		if (erSkjermet) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST)
		}

		endringsmeldingService.opprettMedStartDato(deltakerId, startDato, ansattId)
	}

	data class EndringsmeldingDto(
		val id: UUID,
		val startDato: LocalDate?,
		val aktiv: Boolean,
	)

}
