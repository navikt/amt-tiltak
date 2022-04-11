package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavEnhetTilgang

interface NavAnsattService {

	fun getNavAnsatt(navIdent: String): NavAnsatt

	fun hentEnhetTilganger(navIdent: String): List<NavEnhetTilgang>

}
