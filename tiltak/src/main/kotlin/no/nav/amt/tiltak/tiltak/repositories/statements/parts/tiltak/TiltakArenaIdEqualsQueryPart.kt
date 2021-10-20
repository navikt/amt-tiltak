package no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltak

import no.nav.amt.tiltak.tiltak.repositories.statements.parts.QueryPart

class TiltakArenaIdEqualsQueryPart(
    val arenaId: String
) : QueryPart {

    override fun getTemplate(): String {
        return "tiltak.arena_id = :arenaId"
    }

    override fun getParameters(): Map<String, Any> {
        return mapOf(
            "arenaId" to arenaId
        )
    }
}
