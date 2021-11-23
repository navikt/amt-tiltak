package no.nav.amt.tiltak.tiltak.deltaker.cmd

data class UpsertNavAnsattCmd(
	val personligIdent: String,
	val fornavn: String?,
	val etternavn: String?,
	val telefonnummer: String?,
	val epost: String?
)
