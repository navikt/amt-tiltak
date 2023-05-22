package no.nav.amt.tiltak.clients.amt_person.dto

import java.util.*

data class NavEnhetDto(
	val id: UUID,
	val enhetId: String,
	val navn: String,
)
