package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.connectors.nom.graphql.Error
import no.nav.amt.tiltak.connectors.nom.graphql.GraphqlResponse
import no.nav.amt.tiltak.core.domain.veileder.Veileder

internal data class HentIdenterResponse(
	override val errors: List<Error> = listOf(),
	override val data: NomData
) : GraphqlResponse<NomData> {

	fun toVeiledere(): List<Veileder> {
		errors.forEach { it.logError() }
		return data.ressurser.toVeiledere().filterNotNull()
	}
}
