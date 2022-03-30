package no.nav.amt.tiltak.clients.axsys

interface AxsysClient {

	fun hentTilganger(brukerident: String): Enheter

}

data class Enheter(
	val enheter: List<Enhet> = listOf()
)

data class Enhet(
	val enhetId: String,
	val temaer: List<String>,
	val navn: String
)


