package no.nav.amt.tiltak.tiltaksleverandor.controllers

import no.nav.amt.tiltak.core.port.TiltaksleverandorService
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.AnsattDTO
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.AnsattRolle
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.VirksomhetDTO
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/tiltaksleverandor/ansatt")
class AnsattController(
    private val service: TiltaksleverandorService
) {

	@Protected
    @GetMapping("/meg")
    fun getInnloggetAnsatt(): AnsattDTO {
        return AnsattDTO(
			id = UUID.randomUUID(),
			fornavn = "Navn",
			etternavn = "Navnsen",
			virksomheter = listOf(VirksomhetDTO(
				id = UUID.randomUUID(),
				virksomhetsnummer = "123456789",
				virksomhetsnavn = "Muligheter",
				roller = listOf(AnsattRolle.KOORDINATOR, AnsattRolle.VEILEDER)
			)))
    }

}
