package no.nav.amt.tiltak.deltaker.dbo

import java.time.LocalDateTime
import java.util.*

data class BrukerDbo(
	val id: UUID,
	val fodselsnummer: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val ansvarligVeilederId: UUID?,
	val navEnhetId: UUID?,
	val erSkjermet: Boolean,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
)

