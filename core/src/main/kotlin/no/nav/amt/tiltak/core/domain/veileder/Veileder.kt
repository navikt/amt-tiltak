package no.nav.amt.tiltak.core.domain.veileder

import java.util.*

data class Veileder(
	val id: UUID = UUID.randomUUID(), // Dette vil skape problemer for kode som bruker Veileder fra Nom siden IDen ikke er riktig
	val navIdent: String,
	val navn: String,
	val epost: String?,
	val telefonnummer: String?
)
