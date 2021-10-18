package no.nav.amt.tiltak.connectors.nom.client

import com.fasterxml.jackson.databind.ObjectMapper

private val mapper = ObjectMapper()

internal class HentIdenterRequest(identer: List<String>) {

	private val query: String = """
	{
		"query": "query(${"$"}identer: [String!]!) {
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
		}",
		"variables": { "identer": ${identer.asJson()} }
	}
	""".makeJsonSafe()

	internal fun asJson() = query

	private fun List<String>.asJson() : String = mapper.writeValueAsString(this)

	private fun String.makeJsonSafe() : String = replace("\n", "")
		.replace("\r", "")
		.replace("\t", "")

}
