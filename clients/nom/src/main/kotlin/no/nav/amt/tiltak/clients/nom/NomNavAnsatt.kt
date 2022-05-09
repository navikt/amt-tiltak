package no.nav.amt.tiltak.clients.nom

data class NomNavAnsatt(
	val navIdent: String,
	val navn: String,
	val telefonnummer: String?,
	val epost: String?,
)
