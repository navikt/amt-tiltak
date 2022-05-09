package no.nav.amt.tiltak.clients.nom

class NomClientMock : NomClient {

	override fun hentNavAnsatt(navIdent: String): NomNavAnsatt {
		return NomNavAnsatt(
			navIdent = navIdent,
			navn = "F_$navIdent E_$navIdent",
			epost = "F_$navIdent.E_$navIdent@trygdeetaten.no",
			telefonnummer = "12345678"
		)
	}

}
