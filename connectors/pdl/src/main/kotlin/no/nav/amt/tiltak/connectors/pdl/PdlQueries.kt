package no.nav.amt.tiltak.connectors.pdl

import no.nav.amt.tiltak.tools.graphql.Graphql


object PdlQueries {

	object HentBruker {
		val query = """
			query(${"$"}ident: ID!) {
			  hentPerson(ident: ${"$"}ident) {
				navn(historikk: false) {
				  fornavn
				  mellomnavn
				  etternavn
				}
				telefonnummer {
				  landskode
				  nummer
				  prioritet
				}
			  }
			}
		""".trimIndent()

		data class Variables(
			val ident: String,
		)

		data class Response(
			override val errors: List<GraphqlError>?,
			override val data: ResponseData?
		) : Graphql.GraphqlResponse<ResponseData>

		data class ResponseData(
			val hentPerson: HentPerson,
		)

		data class HentPerson(
			val navn: List<Navn>,
			val telefonnummer: List<Telefonnummer>,
		)

		data class Navn(
			val fornavn: String,
			val mellomnavn: String?,
			val etternavn: String,
		)

		data class Telefonnummer(
			val landskode: String,
			val nummer: String,
			val prioritet: Int,
		)

		data class GraphqlError(
			override val message: String? = null,
			override val locations: List<Graphql.GraphqlErrorLocation>? = null,
			override val path: List<String>? = null,
			val extensions: GraphqlErrorExtensions? = null,
			): Graphql.GraphqlError

		data class GraphqlErrorExtensions(
			val code: String? = null,
			val classification: String? = null,
			val details: GraphqlErrorDetails? = null
		)

		data class GraphqlErrorDetails(
			val type: String? = null,
			val cause: String? = null,
			val policy: String? = null
		)
	}

	object HentGjeldendeIdent {
		val query = """
			query(${"$"}ident: ID!) {
				hentIdenter(ident: ${"$"}ident, grupper: [FOLKEREGISTERIDENT], historikk: false) {
					identer {
						ident
					}
				}
			}
		""".trimIndent()

		data class Variables(
			val ident: String,
		)

		data class Response(
			override val errors: List<Graphql.GraphqlError>?,
			override val data: ResponseData?
		) : Graphql.GraphqlResponse<ResponseData>

		data class ResponseData(
			val hentIdenter: HentIdenter?,
		)

		data class HentIdenter(
			val identer: List<Ident>,
		)

		data class Ident(
			val ident: String,
		)
	}

}
