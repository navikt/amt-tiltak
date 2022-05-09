package no.nav.amt.tiltak.navansatt

import no.nav.amt.tiltak.core.domain.nav_ansatt.Bucket
import java.util.*

internal data class NavAnsattDbo(
	val id: UUID = UUID.randomUUID(),
	val navIdent: String,
	val navn: String,
	val telefonnummer: String?,
	val epost: String?,
	val bucket: Bucket = Bucket.forNavIdent(navIdent)
)
