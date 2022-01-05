package no.nav.amt.tiltak.deltaker.dbo

import java.util.*

data class NavKontorDbo(
	val id: UUID,
	val enhetId: String,
	val navn: String
)
