package no.nav.amt.tiltak.connectors.pdl

interface PdlConnector {

	fun hentBruker(brukerFnr: String): PdlBruker

}

data class PdlBruker(
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
)
