package no.nav.amt.tiltak.connectors.pdl

interface PdlClient {

	fun hentBruker(brukerFnr: String): PdlBruker

	fun hentFnr(aktorId: String): String

}

data class PdlBruker(
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
)
