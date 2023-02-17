package no.nav.amt.tiltak.arrangor

import java.time.LocalDateTime
import java.util.*

data class ArrangorUpdateDbo(
	val id: UUID,
	val navn: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
	val modifiedAt: LocalDateTime = LocalDateTime.now(),
)
