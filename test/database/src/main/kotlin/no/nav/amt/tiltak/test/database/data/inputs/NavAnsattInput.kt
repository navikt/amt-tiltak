package no.nav.amt.tiltak.test.database.data.inputs

import java.util.*

data class NavAnsattInput(
	val id: UUID,
	val navIdent: String,
	val navn: String,
	val telefonnummer: String,
	val epost: String
)
