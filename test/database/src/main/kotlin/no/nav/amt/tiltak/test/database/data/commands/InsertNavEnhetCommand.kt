package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertNavEnhetCommand(
	val id: UUID,
	val enhet_id: String,
	val navn: String
)
