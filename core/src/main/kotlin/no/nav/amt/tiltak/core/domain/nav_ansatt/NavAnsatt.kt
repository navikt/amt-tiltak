package no.nav.amt.tiltak.core.domain.nav_ansatt

import java.util.*

data class NavAnsatt(
	val id: UUID,
	val navIdent: String,
	val navn: String,
	val epost: String? = null,
	val telefonnummer: String? = null,
)
