package no.nav.amt_tiltak.clients.amt_altinn_acl

interface AmtAltinnAclClient {

	fun hentRettigheter(norskIdent: String, rettighetIder: List<String>): List<Rettighet>

}

data class Rettighet(
	val id: String,
	val organisasjonsnummer: String,
)
