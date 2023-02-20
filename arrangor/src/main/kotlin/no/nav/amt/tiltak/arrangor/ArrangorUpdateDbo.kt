package no.nav.amt.tiltak.arrangor

import java.util.*

data class ArrangorUpdateDbo(
	val id: UUID,
	val navn: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
)
