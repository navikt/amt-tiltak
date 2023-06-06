package no.nav.amt.tiltak.clients.amt_person.dto

import java.util.*

data class OpprettNavBrukerDto(
	val id: UUID,
	val personIdent: String,
	val personIdentType: String?,
	val historiskeIdenter: List<String>,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val navVeilederId: UUID?,
	val navEnhetId: UUID?,
	val telefon: String?,
	val epost: String?,
	val erSkjermet: Boolean,
)
