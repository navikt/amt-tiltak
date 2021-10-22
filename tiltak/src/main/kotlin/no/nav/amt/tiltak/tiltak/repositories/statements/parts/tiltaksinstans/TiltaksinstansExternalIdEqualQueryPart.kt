package no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltaksinstans

import no.nav.amt.tiltak.tiltak.repositories.statements.parts.QueryPart
import java.util.*

class TiltaksinstansExternalIdEqualQueryPart(
	private val externalId: UUID
) : QueryPart {
	override fun getTemplate(): String {
		return "tiltaksinstans.external_id = :tiltaksinstansExternalId"
	}

	override fun getParameters(): Map<String, Any> {
		return mapOf(
			"tiltaksinstansExternalId" to externalId
		)
	}
}
