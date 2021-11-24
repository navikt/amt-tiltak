package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.veileder.Veileder

interface NomConnector {

	fun hentVeileder(navIdent: String): Veileder?

}
