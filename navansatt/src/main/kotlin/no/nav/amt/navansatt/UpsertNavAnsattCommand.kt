package no.nav.amt.navansatt

internal data class UpsertNavAnsattCommand(
	val navIdent: String,
	val navn: String,
	val telefonnummer: String?,
	val epost: String?
)
