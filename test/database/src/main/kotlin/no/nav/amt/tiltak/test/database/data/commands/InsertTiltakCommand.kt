package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertTiltakCommand(
	val id: UUID,
	val navn: String,
	val type: String
)
