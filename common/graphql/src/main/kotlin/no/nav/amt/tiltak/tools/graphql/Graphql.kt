package no.nav.amt.tiltak.tools.graphql

object Graphql {

	interface GraphqlError<ErrorExtension> {
		val message: String?
		val locations: List<GraphqlErrorLocation>?
		val path: List<String>?
		val extensions: ErrorExtension?
	}

	data class GraphqlQuery(
		val query: String,
		val variables: Any
	)

	interface GraphqlResponse<Data, ErrorExtension> {
		val errors: List<GraphqlError<ErrorExtension>>?
		val data: Data?
	}

	data class GraphqlErrorLocation(
		val line: Int?,
		val column: Int?,
	)

}
