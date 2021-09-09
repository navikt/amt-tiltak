package no.nav.amt.tiltak.tiltaksleverandor.controllers

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Virksomhet
import no.nav.amt.tiltak.core.port.Tiltaksleverandor
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.AnsattDTO
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.toDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController("/api/tiltaksleverandor/ansatt")
class AnsattController(
    private val service: Tiltaksleverandor
) {

    @GetMapping("/me")
    fun getInnloggetAnsatt(): AnsattDTO {
        val ansattId = UUID.randomUUID()

        return Ansatt(
            id = UUID.randomUUID(),
            fornavn = "Per",
            etternavn = "Testy",
            virksomheter = service.getVirksomheterForAnsatt(ansattId)
        ).toDto()
    }

}
