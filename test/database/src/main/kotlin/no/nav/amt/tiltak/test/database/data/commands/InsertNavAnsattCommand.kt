package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertNavAnsattCommand(
	val id: UUID,
	val navIdent: String,
	val navn: String,
	val telefonnummer: String,
	val epost: String
)
