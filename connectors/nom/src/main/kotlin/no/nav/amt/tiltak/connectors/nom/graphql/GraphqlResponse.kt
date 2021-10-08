package no.nav.amt.tiltak.connectors.nom.graphql

import org.slf4j.LoggerFactory

internal interface GraphqlResponse<D> {
	val errors: List<Error>?
	val data: D?
}

internal data class Error (
	val message: String? = null,
	val locations: List<Location>? = null,
	val path: List<String>? = null,
	val extensions: Extensions? = null
) {

	companion object {
		private val log = LoggerFactory.getLogger(GraphqlResponse::class.java)
	}

	fun logError() {
		log.error("Graphql returnerte med feil: {}", message) // TODO kanskje spisse mer - men da må vi tenke på om alt skal logges på level error
	}

}

internal data class Location (
	val line: Int,
	val column: Int
)

data class Extensions (
	val code: String? = null,
	val classification: String? = null
)
