package no.nav.amt.tiltak.clients.nom

data class NomVeileder(
	val navIdent: String,
	val visningNavn: String?,
	val fornavn: String?,
	val etternavn: String?,
	val telefonnummer: String?,
	val epost: String?,
)
