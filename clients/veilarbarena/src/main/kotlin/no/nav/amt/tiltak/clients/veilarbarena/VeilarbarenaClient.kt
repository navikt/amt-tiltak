package no.nav.amt.tiltak.clients.veilarbarena

interface VeilarbarenaClient {

	fun hentBrukerOppfolgingsenhetId(fnr: String): String?

}
