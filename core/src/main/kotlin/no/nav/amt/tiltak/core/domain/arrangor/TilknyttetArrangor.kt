package no.nav.amt.tiltak.core.domain.arrangor

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import java.util.*

data class TilknyttetArrangor(
	val id: UUID,
	val navn: String,
	val organisasjonsnummer: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?,
	val roller: List<ArrangorAnsattRolle>
)
