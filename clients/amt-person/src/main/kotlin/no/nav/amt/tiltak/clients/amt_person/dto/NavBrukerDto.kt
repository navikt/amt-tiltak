package no.nav.amt.tiltak.clients.amt_person.dto

import java.util.UUID

data class NavBrukerDto (
	val personId: UUID,
	val personident: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val navVeilederId: UUID?,
	val navEnhet: NavEnhetDto?,
	val telefon: String?,
	val epost: String?,
	val erSkjermet: Boolean,
) {
	data class NavEnhetDto(
		val id: UUID,
		val enhetId: String,
		val navn: String,
	)
}
