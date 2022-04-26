package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet

interface NavEnhetConnector {

	fun hentNavEnhetForBruker(fnr: String): NavEnhet?

}
