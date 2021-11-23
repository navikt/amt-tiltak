package no.nav.amt.tiltak.tiltak.deltaker.dbo

data class NavAnsattDbo(
	val id: Int = -1,
	val personligIdent: String,
	val fornavn: String?,
	val etternavn: String?,
	val telefonnummer: String?,
	val epost: String?
)
