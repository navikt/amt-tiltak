package no.nav.amt.tiltak.arrangor

import no.nav.amt.tiltak.ansatt.AnsattRolle
import java.util.*

data class VirksomhetDTO(
	val id: UUID,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
	val roller: List<AnsattRolle>
)

