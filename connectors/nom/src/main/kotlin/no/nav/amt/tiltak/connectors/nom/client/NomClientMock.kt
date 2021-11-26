package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.NomConnector

class NomClientMock : NomConnector {

	override fun hentVeileder(navIdent: String): Veileder? {
		return Veileder(
			navIdent = navIdent,
			fornavn = "F_$navIdent",
			etternavn = "E_$navIdent",
			visningsNavn = "F_$navIdent E_$navIdent",
			epost = "F_$navIdent.E_$navIdent@trygdeetaten.no",
		)
	}

}
