package no.nav.amt.tiltak.tiltaksleverandor.controllers.dto

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import java.util.*

data class AnsattDTO(
    val id: UUID
)

fun Ansatt.toDto(): AnsattDTO {
    return AnsattDTO(
        id = this.id
    )
}
