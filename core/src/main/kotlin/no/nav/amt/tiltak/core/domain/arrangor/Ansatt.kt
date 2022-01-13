package no.nav.amt.tiltak.core.domain.arrangor

import java.util.*

data class Ansatt (
    val id: UUID,
	val personligIdent: String,
    val fornavn: String,
    val etternavn: String,
    val arrangorer: List<TilknyttetArrangor>
)
