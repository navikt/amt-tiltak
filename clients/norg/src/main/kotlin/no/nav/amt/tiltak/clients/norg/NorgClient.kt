package no.nav.amt.tiltak.clients.norg

interface NorgClient {

	fun hentNavKontorNavn(enhetId: String): String

}
