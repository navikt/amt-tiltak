package no.nav.amt.tiltak.clients.amt_person_service.dto

import java.util.*

data class NavBrukerDto (
	val id: UUID,
	val personIdent: String,
	val personIdentType: String?,
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
