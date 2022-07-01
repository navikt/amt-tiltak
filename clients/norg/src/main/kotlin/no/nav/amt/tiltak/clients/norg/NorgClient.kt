package no.nav.amt.tiltak.clients.norg

interface NorgClient {

	fun hentNavEnhet(enhetId: String): NorgNavEnhet?

	fun hentAlleNavEnheter(): List<NorgNavEnhet>

}

data class NorgNavEnhet(
	val enhetId: String,
	val navn: String
)
