package no.nav.amt.tiltak.connectors.norg

interface NorgConnector {

	fun hentNavKontorNavn(enhetId: String): String

}
