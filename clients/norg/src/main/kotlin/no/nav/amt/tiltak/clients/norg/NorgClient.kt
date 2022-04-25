package no.nav.amt.tiltak.clients.norg

interface NorgClient {

	fun hentNavEnhetNavn(enhetId: String): String

	fun hentAlleNavEnheter(): List<NorgNavEnhet>

}

data class NorgNavEnhet(
	val enhetId: String,
	val navn: String
)
