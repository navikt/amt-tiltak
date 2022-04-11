package no.nav.amt.tiltak.core.domain.nav_ansatt

import java.util.*

data class NavAnsatt(
	val id: UUID = UUID.randomUUID(), // Burde ikke ha en randomUUID() som default. Nom burde istedenfor ikke returnere NavAnastt
	val navIdent: String,
	val navn: String,
	val epost: String? = null,
	val telefonnummer: String? = null,
)
