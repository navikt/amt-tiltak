package no.nav.amt.tiltak.clients.amt_person_service.dto

import java.util.*

data class NavAnsattDto(
	val id: UUID,
	val navIdent: String,
	val navn: String,
	val epost: String?,
	val telefon: String?,
)
