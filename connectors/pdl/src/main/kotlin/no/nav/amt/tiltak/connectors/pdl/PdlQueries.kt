package no.nav.amt.tiltak.connectors.pdl


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
			override val errors: List<Graphql.GraphqlError>?,
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

	}

}
