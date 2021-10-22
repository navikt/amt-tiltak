package no.nav.amt.tiltak.tiltaksleverandor.repositories.statements.parts

import java.util.*

class TiltaksleverandorExternalIdEqualsQueryPart(
	private val externalId: UUID
) : QueryPart {

	override fun getTemplate(): String {
		return """
			tiltaksleverandor.external_id = :tiltaksleverandorExternalId
		""".trimIndent()
	}

	override fun getParameters(): Map<String, Any> {
		return mapOf(
			"tiltaksleverandorExternalId" to externalId
		)
	}
}
