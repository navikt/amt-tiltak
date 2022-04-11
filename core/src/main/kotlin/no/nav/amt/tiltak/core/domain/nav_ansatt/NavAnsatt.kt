package no.nav.amt.tiltak.core.domain.nav_ansatt

data class NavAnsatt(
	val navIdent: String,
	val navn: String,
	val epost: String? = null,
	val telefonnummer: String? = null,
)
