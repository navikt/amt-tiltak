package no.nav.amt.tiltak.core.domain.tiltaksleverandor

import java.util.*

data class Virksomhet(
	val id: UUID? = null,
	val organisasjonsnummer: String,
	val organisasjonsnavn: String,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
)
