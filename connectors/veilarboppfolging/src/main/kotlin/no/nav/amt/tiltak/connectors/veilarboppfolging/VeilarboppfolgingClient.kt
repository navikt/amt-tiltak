package no.nav.amt.tiltak.connectors.veilarboppfolging

import no.nav.amt.tiltak.core.port.VeilarboppfolgingConnector

class VeilarboppfolgingClient : VeilarboppfolgingConnector {
	override fun hentVeilederIdent(fnr: String) : String {
		return ""
	}
}
