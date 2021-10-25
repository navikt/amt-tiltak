package no.nav.amt.tiltak.tiltak.repositories.statements.parts.deltaker

import no.nav.amt.tiltak.tiltak.repositories.statements.parts.QueryPart
import java.util.*

class DeltakerExternalIdEqualsQueryPart(
	private val externalId: UUID
) : QueryPart {
	override fun getTemplate(): String {
		return "deltaker.external_id = :deltakerExternalId"
	}

	override fun getParameters(): Map<String, Any> {
		return mapOf(
			"deltakerExternalId" to externalId
		)
	}
}
