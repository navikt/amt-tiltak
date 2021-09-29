package no.nav.amt.tiltak.connectors.nom

import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.NomConnector

class NomConnectorImpl: NomConnector {

	override fun hentVeileder(ident: String) : Veileder {
		return Veileder("", "", "", "", "", "")
	}

}
