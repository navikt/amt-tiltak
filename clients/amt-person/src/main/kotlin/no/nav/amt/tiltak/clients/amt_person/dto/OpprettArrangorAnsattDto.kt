package no.nav.amt.tiltak.clients.amt_person.dto

import java.util.*

data class OpprettArrangorAnsattDto(
	val id: UUID,
	val personIdent: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
)
