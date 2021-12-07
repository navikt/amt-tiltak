package no.nav.amt.tiltak.deltaker.dbo

import java.util.*

data class NavAnsattDbo(
	val id: UUID,
	val personligIdent: String,
	val navn: String,
	val telefonnummer: String?,
	val epost: String?
)
