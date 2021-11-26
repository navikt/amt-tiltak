package no.nav.amt.tiltak.core.domain.tiltaksleverandor

import java.util.*

data class TilknyttetLeverandor(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
	val roller: List<String>
)
