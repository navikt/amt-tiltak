package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertArrangorAnsattCommand(
	val id: UUID,
	val personlig_ident: String,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String
)
