package no.nav.amt.tiltak.core.domain.tiltaksarrangor

import java.util.*

data class Ansatt (
    val id: UUID,
	val personligIdent: String,
    val fornavn: String,
    val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
    val arrangorer: List<TilknyttetArrangor>
)
