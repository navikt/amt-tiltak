package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertBrukerCommand(
	val id: UUID,
	val fodselsnummer: String,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val telefonnummer: String,
	val epost: String,
	val ansvarligVeilederId: UUID?,
	val navEnhetId: UUID
)

