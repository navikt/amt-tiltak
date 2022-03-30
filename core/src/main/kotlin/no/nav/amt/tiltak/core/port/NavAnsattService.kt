package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.navansatt.NavAnsatt

interface NavAnsattService {
	// TODO kan (og burde?) denne slås sammen med tiltaks-arrangør sin tilgangservice? InnloggetBrukerService kanskje?
	fun getNavAnsatt(navIdent: String): NavAnsatt

}
