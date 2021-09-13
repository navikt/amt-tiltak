package no.nav.amt.tiltak.tiltaksleverandor.controllers.dto

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import java.util.*

data class AnsattDTO(
    val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val virksomheter: List<VirksomhetDTO>
)

fun Ansatt.toDto(): AnsattDTO {
    return AnsattDTO(
        id = this.id,
    	fornavn = this.fornavn,
		mellomnavn = this.mellomnavn,
		etternavn = this.etternavn,
		virksomheter = emptyList()
    )
}
