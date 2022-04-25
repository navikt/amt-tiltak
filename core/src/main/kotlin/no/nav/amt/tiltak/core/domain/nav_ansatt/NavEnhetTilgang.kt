package no.nav.amt.tiltak.core.domain.nav_ansatt

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet

data class NavEnhetTilgang(
	val enhet: NavEnhet,
	val temaer: List<String>
)
