package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.NavKontor

interface NavKontorConnector {

	fun hentNavKontorForBruker(fnr: String): NavKontor?

}
