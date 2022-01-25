package no.nav.amt.tiltak.connectors.veilarbarena

interface VeilarbarenaConnector {

	fun hentBrukerOppfolgingsenhetId(fnr: String): String?

}
