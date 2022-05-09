package no.nav.amt.tiltak.core.domain.nav_ansatt

import java.util.*

data class UpsertNavAnsattInput(
	val id: UUID,
	val navIdent: String,
	val navn: String,
	val telefonnummer: String?,
	val epost: String?,
	val bucket: Bucket = Bucket.forNavIdent(navIdent)
)
