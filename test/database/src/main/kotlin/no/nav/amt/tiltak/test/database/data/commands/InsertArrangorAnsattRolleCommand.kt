package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertArrangorAnsattRolleCommand(
	val id: UUID,
	val arrangor_id: UUID,
	val ansatt_id: UUID,
	val rolle: String
)
