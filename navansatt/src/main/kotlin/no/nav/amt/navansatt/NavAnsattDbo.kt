package no.nav.amt.navansatt

import java.util.*

internal data class NavAnsattDbo(
	val id: UUID = UUID.randomUUID(),
	val navIdent: String,
	val navn: String,
	val telefonnummer: String?,
	val epost: String?
) {
	val bucket = NavAnsattBucket.forUuid(id)
}
