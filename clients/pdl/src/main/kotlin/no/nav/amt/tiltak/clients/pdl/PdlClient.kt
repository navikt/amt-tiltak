package no.nav.amt.tiltak.clients.pdl

interface PdlClient {

	fun hentBruker(brukerFnr: String): PdlBruker

	fun hentGjeldendePersonligIdent(ident: String): String

}

data class PdlBruker(
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
	val adressebeskyttelseGradering: AdressebeskyttelseGradering?
)

enum class AdressebeskyttelseGradering {
	STRENGT_FORTROLIG,
	FORTROLIG,
	STRENGT_FORTROLIG_UTLAND,
}
