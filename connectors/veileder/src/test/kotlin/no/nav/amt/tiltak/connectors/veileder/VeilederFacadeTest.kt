package no.nav.amt.tiltak.connectors.veileder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.connectors.nom.client.NomClient
import no.nav.amt.tiltak.connectors.nom.client.NomVeileder

class VeilederFacadeTest : FunSpec({

	test("Skal bruke visningsnavn fra NOM hvis tilgjengelig") {
		val nomClient = object : NomClient {
			override fun hentVeileder(navIdent: String): NomVeileder {
				return NomVeileder(
					navIdent = navIdent,
					visningNavn = "visning navn",
					fornavn = "fornavn",
					etternavn = "etternavn",
					epost = null,
					telefonnummer = null
				)
			}
		}

		val veilederFacade = VeilederFacade(nomClient)
		val veileder = veilederFacade.hentVeileder("test")

		veileder?.navn shouldBe "visning navn"
	}

	test("Skal lage navn hvis visningsnavn fra NOM ikke er tilgjengelig") {
		val nomClient = object : NomClient {
			override fun hentVeileder(navIdent: String): NomVeileder {
				return NomVeileder(
					navIdent = navIdent,
					visningNavn = null,
					fornavn = "fornavn",
					etternavn = "etternavn",
					epost = null,
					telefonnummer = null
				)
			}
		}

		val veilederFacade = VeilederFacade(nomClient)
		val veileder = veilederFacade.hentVeileder("test")

		veileder?.navn shouldBe "fornavn etternavn"
	}

})
