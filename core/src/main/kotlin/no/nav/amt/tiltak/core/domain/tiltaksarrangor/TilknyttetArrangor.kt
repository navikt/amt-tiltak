package no.nav.amt.tiltak.core.domain.tiltaksarrangor

import java.util.*

data class TilknyttetArrangor(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
	val roller: List<String>
)
