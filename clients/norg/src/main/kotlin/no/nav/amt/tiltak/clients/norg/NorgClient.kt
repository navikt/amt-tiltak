package no.nav.amt.tiltak.clients.norg

interface NorgClient {

	fun hentNavKontorNavn(enhetId: String): String

	fun hentAlleNavKontorer(): List<NorgNavKontor>

}

data class NorgNavKontor(
	val enhetId: String,
	val navn: String
)
