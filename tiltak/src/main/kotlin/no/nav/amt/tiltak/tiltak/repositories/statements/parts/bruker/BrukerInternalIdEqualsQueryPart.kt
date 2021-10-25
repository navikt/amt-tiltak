package no.nav.amt.tiltak.tiltak.repositories.statements.parts.bruker

import no.nav.amt.tiltak.tiltak.repositories.statements.parts.QueryPart

class BrukerInternalIdEqualsQueryPart(
	private val brukerId: Int
) : QueryPart {

	override fun getTemplate(): String {
		return "bruker.id = :brukerId"
	}

	override fun getParameters(): Map<String, Any> {
		return mapOf(
			"brukerId" to brukerId
		)
	}

}
