package no.nav.amt.tiltak.core.domain.nav_ansatt

import no.nav.amt.tiltak.core.domain.tiltak.NavKontor

data class NavEnhetTilgang(
	val kontor: NavKontor,
	val temaer: List<String>
)
