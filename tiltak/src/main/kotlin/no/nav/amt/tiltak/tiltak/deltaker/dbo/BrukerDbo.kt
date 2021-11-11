package no.nav.amt.tiltak.tiltak.deltaker.dbo

import java.time.LocalDateTime

data class BrukerDbo(
	val internalId: Int,
	val fodselsnummer: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val ansvarligVeilederInternalId: Int?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
)
