package no.nav.amt.tiltak.clients.amt_enhetsregister

interface EnhetsregisterClient {

	fun hentVirksomhet(organisasjonsnummer: String): Virksomhet

}

data class Virksomhet(
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String? = null,
	val overordnetEnhetNavn: String? = null,
)
