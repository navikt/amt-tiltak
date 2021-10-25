package no.nav.amt.tiltak.tiltak.repositories.statements.parts.bruker

import no.nav.amt.tiltak.tiltak.repositories.statements.parts.QueryPart

class BrukerFodselsnummerEqualsQueryPart(
	val fodselsnummer: String
) : QueryPart {

	override fun getTemplate(): String {
		return "bruker.fodselsnummer = :bruker_fodselsnummer"
	}

	override fun getParameters(): Map<String, Any> {
		return mapOf(
			"bruker_fodselsnummer" to fodselsnummer
		)
	}
}
