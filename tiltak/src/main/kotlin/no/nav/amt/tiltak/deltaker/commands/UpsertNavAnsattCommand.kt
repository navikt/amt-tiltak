package no.nav.amt.tiltak.deltaker.commands

data class UpsertNavAnsattCommand(
	val navIdent: String,
	val navn: String,
	val telefonnummer: String?,
	val epost: String?
)
