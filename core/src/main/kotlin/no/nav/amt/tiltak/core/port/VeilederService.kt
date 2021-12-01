package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.veileder.Veileder

interface VeilederService {

	fun hentVeileder(navIdent: String): Veileder?

}
