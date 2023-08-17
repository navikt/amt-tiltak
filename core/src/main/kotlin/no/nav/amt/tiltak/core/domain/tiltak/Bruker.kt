package no.nav.amt.tiltak.core.domain.tiltak

import java.util.UUID

data class Bruker(
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
	val adresse: Adresse?
)
