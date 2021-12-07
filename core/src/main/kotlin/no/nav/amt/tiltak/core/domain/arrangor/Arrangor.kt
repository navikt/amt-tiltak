package no.nav.amt.tiltak.core.domain.arrangor

import java.util.*

data class Arrangor(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
)
