package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Adresse
import java.time.LocalDateTime
import java.util.UUID

data class BrukerDbo(
	val id: UUID,
	val personIdent: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val ansvarligVeilederId: UUID?,
	val navEnhetId: UUID?,
	val erSkjermet: Boolean,
	val adresse: Adresse?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
)

