package no.nav.amt.tiltak.core.port

interface VeilarbarenaConnector {

	fun hentBrukerArenaStatus(fnr: String): BrukerArenaStatus

}

data class BrukerArenaStatus(
	var oppfolgingsenhetId: String?
)
