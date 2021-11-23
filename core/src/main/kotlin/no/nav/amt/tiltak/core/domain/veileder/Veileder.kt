package no.nav.amt.tiltak.core.domain.veileder

import java.util.*

data class Veileder(
	val id: UUID? = null,
	val navIdent: String,
	val fornavn: String,
	val etternavn: String,
	val epost: String,
)
