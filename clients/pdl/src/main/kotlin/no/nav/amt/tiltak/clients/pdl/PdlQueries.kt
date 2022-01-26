package no.nav.amt.tiltak.clients.pdl

import no.nav.amt.tiltak.tools.graphql.Graphql


object PdlQueries {

	data class PdlError (
		override val message: String? = null,
		override val locations: List<Graphql.GraphqlErrorLocation>? = null,
		override val path: List<String>? = null,
		override val extensions: PdlErrorExtension? = null,
	): Graphql.GraphqlError<PdlErrorExtension>

	data class PdlErrorExtension(
		val code: String? = null,
		val classification: String? = null,
		val details: PdlErrorDetails? = null
	)

	data class PdlErrorDetails(
		val type: String? = null,
		val cause: String? = null,
		val policy: String? = null
	)

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
				adressebeskyttelse(historikk: false) {
				  gradering
				}
			  }
			}
		""".trimIndent()

		data class Variables(
			val ident: String,
		)

		data class Response(
			override val errors: List<PdlError>?,
			override val data: ResponseData?
		) : Graphql.GraphqlResponse<ResponseData, PdlErrorExtension>

		data class ResponseData(
			val hentPerson: HentPerson,
		)

		data class HentPerson(
			val navn: List<Navn>,
			val telefonnummer: List<Telefonnummer>,
			val adressebeskyttelse: List<Adressebeskyttelse>
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

		data class Adressebeskyttelse(
			val gradering: String
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
			override val errors: List<PdlError>?,
			override val data: ResponseData?
		) : Graphql.GraphqlResponse<ResponseData, PdlErrorExtension>

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
