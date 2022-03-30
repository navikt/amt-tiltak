package no.nav.amt.tiltak.core.domain.arrangor

import java.util.*

data class Avtale(
	private val id: UUID,
	private val avtalenavn: String,
	private val ansvarligEnhet: String
)
