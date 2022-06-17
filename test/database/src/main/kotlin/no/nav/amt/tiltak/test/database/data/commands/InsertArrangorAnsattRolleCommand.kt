package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertArrangorAnsattRolleCommand(
	val id: UUID,
	val arrangorId: UUID,
	val ansattId: UUID,
	val rolle: String
)
