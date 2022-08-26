package no.nav.amt.tiltak.clients.amt_altinn_acl

interface AmtAltinnAclClient {

	fun hentRettigheter(norskIdent: String, rettighetIder: List<String>): List<AltinnRettighet>

}

data class AltinnRettighet(
	val id: String,
	val organisasjonsnummer: String,
)
