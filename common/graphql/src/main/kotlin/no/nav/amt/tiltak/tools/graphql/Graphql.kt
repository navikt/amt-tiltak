package no.nav.amt.tiltak.tools.graphql

object Graphql {

	interface GraphqlError {
		val message: String?
		val locations: List<GraphqlErrorLocation>?
		val path: List<String>?
	}

	data class GraphqlQuery(
		val query: String,
		val variables: Any
	)

	interface GraphqlResponse<D> {
		val errors: List<GraphqlError>?
		val data: D?
	}

	data class GraphqlErrorLocation(
		val line: Int?,
		val column: Int?,
	)

}
