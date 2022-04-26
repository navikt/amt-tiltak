package no.nav.amt.tiltak.connectors.veileder

import no.nav.amt.tiltak.clients.nom.NomClient
import no.nav.amt.tiltak.clients.nom.NomVeileder
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.VeilederConnector
import org.springframework.stereotype.Service

@Service
class VeilederFacade(
	private val nomClient: NomClient
) : VeilederConnector {

	override fun hentVeileder(navIdent: String): NavAnsatt? {
		return nomClient.hentVeileder(navIdent)?.tilVeileder()
	}

	private fun NomVeileder.tilVeileder(): NavAnsatt {
		val navn = this.visningNavn ?: "${this.fornavn} ${this.etternavn}"

		return NavAnsatt(
			navIdent = this.navIdent,
			navn = navn,
			epost = this.epost,
			telefonnummer = this.telefonnummer,
		)
	}


}
