package no.nav.amt.tiltak.deltaker.controllers

import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.deltaker.service.EndringsmeldingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.apache.commons.lang3.NotImplementedException
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping(value = ["/api/tiltakarrangor/endringsmelding"])
class EndringsmeldingArrangorController(
	private val service: EndringsmeldingService
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PostMapping("/deltaker/{deltakerId}")
	fun setStartDato(@PathVariable("deltakerId") deltakerId: UUID, @RequestParam startDato: LocalDate) {
		throw NotImplementedException("Not implemented yet")
	}
}
