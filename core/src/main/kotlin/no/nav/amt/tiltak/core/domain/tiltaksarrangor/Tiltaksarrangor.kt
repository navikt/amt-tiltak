package no.nav.amt.tiltak.core.domain.tiltaksarrangor

import java.util.*

data class Tiltaksarrangor(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
)
