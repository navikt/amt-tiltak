package no.nav.amt.tiltak.core.domain.tiltak

import java.util.*

class Tiltak(
	val id: UUID?,
	val tiltaksleverandorId: UUID,
	val kode: String,
	val navn: String
)
