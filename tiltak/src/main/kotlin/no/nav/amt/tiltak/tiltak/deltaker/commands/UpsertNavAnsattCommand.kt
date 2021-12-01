package no.nav.amt.tiltak.tiltak.deltaker.commands

data class UpsertNavAnsattCommand(
	val personligIdent: String,
	val navn: String,
	val telefonnummer: String?,
	val epost: String?
)
