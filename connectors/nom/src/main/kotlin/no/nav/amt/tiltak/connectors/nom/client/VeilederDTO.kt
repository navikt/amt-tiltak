package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.core.domain.veileder.Veileder

internal data class VeilederDTO (
	val nomId: String, //Trenger??
	val navIdent: String,

	val fornavn: String,
	val mellomnavn: String,
	val etternavn: String,

	val telefon: String,
	val epost: String,
	//val koblinger: RessursKobling == nav enhet??
){

	fun toVeileder() : Veileder {
		return Veileder(
			this.navIdent,
			this.fornavn,
			this.mellomnavn,
			this.etternavn,
			this.telefon,
			this.epost
		)
	}
}

