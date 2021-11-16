package no.nav.amt.tiltak.core.port

interface NavKontorService {

	fun hentNavKontorForBruker(fnr: String): NavKontor?

}

data class NavKontor(
	val enhetId: String,
	val navn: String,
)
