package no.nav.amt.tiltak.core.domain.nav_ansatt

data class NavEnhetTilgang(
	val enhetId: String,
	val enhetNavn: String,
	val temaer: List<String>,
)
