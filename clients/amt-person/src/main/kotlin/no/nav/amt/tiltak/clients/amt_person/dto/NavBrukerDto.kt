package no.nav.amt.tiltak.clients.amt_person.dto

import no.nav.amt.tiltak.core.domain.tiltak.Adresse
import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse
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
	val adresse: Adresse?,
	val adressebeskyttelse: Adressebeskyttelse? = null
) {
	data class NavEnhetDto(
		val id: UUID,
		val enhetId: String,
		val navn: String,
	)
}
