package no.nav.amt.tiltak.test.database.data.inputs

import java.util.*

data class ArrangorAnsattRolleInput(
	val id: UUID,
	val arrangorId: UUID,
	val ansattId: UUID,
	val rolle: String
)
