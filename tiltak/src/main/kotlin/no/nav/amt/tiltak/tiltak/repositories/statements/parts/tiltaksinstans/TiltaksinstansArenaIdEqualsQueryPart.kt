package no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltaksinstans

import no.nav.amt.tiltak.tiltak.repositories.statements.parts.QueryPart

class TiltaksinstansArenaIdEqualsQueryPart(
	val arenaId: Int
) : QueryPart {
	override fun getTemplate(): String {
		return "tiltaksinstans.arena_id = :arenaId"
	}

	override fun getParameters(): Map<String, Any> {
		return mapOf(
			"arenaId" to arenaId
		)
	}
}
