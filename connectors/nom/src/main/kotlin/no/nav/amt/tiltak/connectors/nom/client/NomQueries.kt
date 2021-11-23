package no.nav.amt.tiltak.connectors.nom.client


object NomQueries {

	object HentIdenter {
		val query = """
			query(${"$"}identer: [String!]!) {
				ressurser(where: { navIdenter: ${"$"}identer }){
					code
					ressurs {
						navIdent
						visningsNavn
						fornavn
						etternavn
						epost
					}
				}
			}
		""".trimIndent()

		data class Variables(
			val identer: List<String>,
		)

		data class Response(
			override val errors: List<Graphql.GraphqlError>?,
			override val data: ResponseData?
		) : Graphql.GraphqlResponse<ResponseData>

		data class ResponseData(
			val ressurser: List<RessursResult>,
		)

		enum class ResultCode {
			OK,
			NOT_FOUND,
			ERROR
		}

		data class RessursResult(
			val code: ResultCode,
			val ressurs: Ressurs?,
		)

		data class Ressurs(
			val navIdent: String,
			val visningsNavn: String?,
			val fornavn: String?,
			val etternavn: String?,
			val epost: String?,
		)

	}

}
