package no.nav.amt.tiltak.tiltaksleverandor.repositories.statements.parts

class TiltaksleverandorVirksomhetsnummerEqualsQueryPart(
	private val virksomhetsnummer: String
) : QueryPart {

	override fun getTemplate(): String {
		return """
			tiltaksleverandor.virksomhetsnummer = :virksomhetsnummer
		""".trimIndent()
	}

	override fun getParameters(): Map<String, Any> {
		return mapOf(
			"virksomhetsnummer" to virksomhetsnummer
		)
	}
}

