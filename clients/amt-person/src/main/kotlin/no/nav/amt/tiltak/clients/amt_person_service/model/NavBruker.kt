package no.nav.amt.tiltak.clients.amt_person_service.model

import java.util.*

data class NavBruker(
	val id: UUID,
	val personIdent: String,
	val personIdentType: IdentType?,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val navVeilederId: UUID?,
	val navEnhet: NavEnhet?,
	val telefon: String?,
	val epost: String?,
	val erSkjermet: Boolean,
) {
	enum class IdentType{
		FOLKEREGISTERIDENT, NPID
	}
	data class NavEnhet(
		val id: UUID,
		val enhetId: String,
		val navn: String,
	)
}
