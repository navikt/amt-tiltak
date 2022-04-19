package no.nav.amt.tiltak.core.domain.tiltak

import java.util.*

data class NavKontor(
	val id: UUID,
	val enhetId: String,
	val navn: String,
)
