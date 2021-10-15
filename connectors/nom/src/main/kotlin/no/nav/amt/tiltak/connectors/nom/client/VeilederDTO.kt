package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.core.domain.veileder.Veileder

internal data class VeilederDTO (
	val navIdent: String?,
	val fornavn: String?,
	val etternavn: String?,
	val epost: String?,
){

	internal fun toVeileder() : Veileder {
		// TODO validering og mer fornuftig konvertering istedet for  ?: "". Eller?
		return Veileder(
			this.navIdent ?: "",
			this.fornavn ?: "",
			this.etternavn ?: "",
			this.epost ?: "",
		)
	}
}

