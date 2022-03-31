package no.nav.amt.tiltak.core.domain.veileder

import java.util.*

data class Veileder(
	val id: UUID = UUID.randomUUID(), // Dette kommer til å skape problemer senere
	val navIdent: String,
	val navn: String,
	val epost: String?,
	val telefonnummer: String?
)
