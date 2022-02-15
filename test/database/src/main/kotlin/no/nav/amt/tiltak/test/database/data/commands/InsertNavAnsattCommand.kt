package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertNavAnsattCommand(
	val id: UUID,
	val nav_ident: String,
	val navn: String,
	val telefonnummer: String,
	val epost: String
)
