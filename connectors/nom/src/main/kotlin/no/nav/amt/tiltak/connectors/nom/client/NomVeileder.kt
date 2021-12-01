package no.nav.amt.tiltak.connectors.nom.client

data class NomVeileder(
	val navIdent: String,
	val visningNavn: String?,
	val fornavn: String?,
	val etternavn: String?,
	val epost: String?,
)
