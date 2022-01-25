package no.nav.amt.tiltak.connectors.nom.client

class NomClientMock : NomClient {

	override fun hentVeileder(navIdent: String): NomVeileder {
		return NomVeileder(
			navIdent = navIdent,
			fornavn = "F_$navIdent",
			etternavn = "E_$navIdent",
			visningNavn = "F_$navIdent E_$navIdent",
			epost = "F_$navIdent.E_$navIdent@trygdeetaten.no",
			telefonnummer = "12345678"
		)
	}

}
