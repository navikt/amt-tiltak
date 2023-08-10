package no.nav.amt.tiltak.navansatt

import java.util.*

internal data class NavAnsattDbo(
	val id: UUID,
	val navIdent: String,
	val navn: String,
	val telefonnummer: String?,
	val epost: String?,
)
