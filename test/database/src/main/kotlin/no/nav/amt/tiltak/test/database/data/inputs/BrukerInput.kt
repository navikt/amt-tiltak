package no.nav.amt.tiltak.test.database.data.inputs

import java.util.*

data class BrukerInput(
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

