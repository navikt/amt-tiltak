package no.nav.amt.tiltak.test.database.data.inputs

import java.util.*

data class ArrangorAnsattInput(
	val id: UUID,
	val personligIdent: String,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String
)
