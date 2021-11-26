package no.nav.amt.tiltak.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Fodselsnummer
import java.time.LocalDateTime
import java.util.*

data class BrukerDbo(
	val id: UUID,
	val fodselsnummer: Fodselsnummer,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val ansvarligVeilederId: UUID?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
)
