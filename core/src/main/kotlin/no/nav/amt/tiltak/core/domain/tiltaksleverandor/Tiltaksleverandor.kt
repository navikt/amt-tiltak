package no.nav.amt.tiltak.core.domain.tiltaksleverandor

import java.util.*

data class Tiltaksleverandor(
	val id: UUID,
	val organisasjonsnummer: String,
	val organisasjonsnavn: String,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
)
