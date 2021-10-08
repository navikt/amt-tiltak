package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.connectors.nom.graphql.Error
import no.nav.amt.tiltak.connectors.nom.graphql.GraphqlResponse
import no.nav.amt.tiltak.core.domain.veileder.Veileder


internal data class HentIdenterResponse(
	override val errors: List<Error> = listOf(),
	override val data: NomData
) : GraphqlResponse<NomData> {

	fun veiledere(): List<Veileder> {
		errors.forEach { it.logError() }
		return data.ressurser.tilVeiledere()
	}

}

internal data class NomData(
	val ressurser: Ressurser
)

internal data class Ressurser(
	private val ressurser: List<RessursResult>
) {
	internal fun tilVeiledere() = ressurser
		.filter { it.isOk() } // TODO Skal vi håndtere mangler og feil på ressursernivå? - kanskje flytte til RessursResult?
		.map { it.convert() }
}

internal data class RessursResult (
	private val code: ResultCode,
	private val ressurs: VeilederDTO
) {
	fun isOk(): Boolean = code == ResultCode.OK

	fun convert() = ressurs.toVeileder()
}

internal enum class ResultCode {
	OK,
	NOT_FOUND,
	ERROR
}


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
