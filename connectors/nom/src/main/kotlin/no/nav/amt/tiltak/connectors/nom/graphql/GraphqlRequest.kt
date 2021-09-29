package no.nav.amt.tiltak.connectors.nom.graphql

import com.fasterxml.jackson.databind.ObjectMapper

val nomQuery: String = """
		query(${"$"}identer: [String!]!) {
		    ressurser(where: { navIdenter: ${"$"}identer }){
				code
		        id
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

val mapper = ObjectMapper()

class NomGraphqlRequest(
	var query: String = nomQuery,
	var variables: List<String>
) {
	fun toJson() = mapper.writeValueAsString(this)
}
