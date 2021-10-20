package no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltak

import no.nav.amt.tiltak.tiltak.repositories.statements.parts.QueryPart
import java.util.*

class TiltakExternalIdEqualsQueryPart(
    private val externalId: UUID
) : QueryPart {

    override fun getTemplate(): String {
        return "tiltak.external_id = :tiltakExternalId"
    }

    override fun getParameters(): Map<String, Any> {
        return mapOf(
            "tiltakExternalId" to externalId
        )
    }
}
