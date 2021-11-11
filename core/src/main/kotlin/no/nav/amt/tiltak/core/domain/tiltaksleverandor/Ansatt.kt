package no.nav.amt.tiltak.core.domain.tiltaksleverandor

import java.util.*

data class Ansatt (
    val id: UUID,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val virksomheter: List<Tiltaksleverandor>?
)
