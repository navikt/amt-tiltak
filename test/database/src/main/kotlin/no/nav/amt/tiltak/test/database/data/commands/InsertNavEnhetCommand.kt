package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertNavEnhetCommand(
	val id: UUID,
	val enhetId: String,
	val navn: String
)
