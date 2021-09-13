package no.nav.amt.tiltak.tiltaksleverandor.controllers

import no.nav.amt.tiltak.core.port.Tiltaksleverandor
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.AnsattDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/api/tiltaksleverandor/ansatt")
class AnsattController(
    private val service: Tiltaksleverandor
) {

    @GetMapping("/me")
    fun getInnloggetAnsatt(): AnsattDTO {
        TODO()
    }

}
