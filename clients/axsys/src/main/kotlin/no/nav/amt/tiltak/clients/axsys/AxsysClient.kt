package no.nav.amt.tiltak.clients.axsys

interface AxsysClient {

	fun hentTilganger(navIdent: String): List<EnhetTilgang>

}

data class EnhetTilgang(
	val enhetId: String,
	val temaer: List<String>,
	val navn: String
)


