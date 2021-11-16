package no.nav.amt.tiltak.connectors.veilarbarena

interface VeilarbarenaConnector {

	fun hentBrukerArenaStatus(fnr: String): BrukerArenaStatus

}

data class BrukerArenaStatus(
	var oppfolgingsenhetId: String?
)
