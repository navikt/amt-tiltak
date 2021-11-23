package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.connectors.nom.graphql.GraphqlResponse
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import org.slf4j.LoggerFactory

internal data class VeilederDTO (
	val navIdent: String?,
	val fornavn: String?,
	val etternavn: String?,
	val epost: String?,
){

	companion object {
		private val log = LoggerFactory.getLogger(GraphqlResponse::class.java)
	}

	internal fun toVeileder() : Veileder {

		if(fornavn == null && etternavn == null) log.error("Fikk veileder($navIdent) uten navn fra NOM.")

		return Veileder(
			id = null,
			this.navIdent ?: "",
			this.fornavn ?: "",
			this.etternavn ?: "",
			this.epost ?: "",
		)
	}
}

