package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt

interface VeilederConnector {

	fun hentVeileder(navIdent: String): NavAnsatt?

}
