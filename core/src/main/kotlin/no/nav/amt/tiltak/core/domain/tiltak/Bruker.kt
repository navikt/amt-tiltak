package no.nav.amt.tiltak.core.domain.tiltak

import java.util.*

data class Bruker(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val fodselsnummer: String,
	val navEnhet: NavEnhet?,
	val navVeilederId: UUID?,
)
