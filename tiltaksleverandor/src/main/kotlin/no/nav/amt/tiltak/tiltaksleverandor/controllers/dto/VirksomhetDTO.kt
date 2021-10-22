package no.nav.amt.tiltak.tiltaksleverandor.controllers.dto

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Tiltaksleverandor
import java.util.*

data class VirksomhetDTO(
	val id: UUID,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
	val roller: List<AnsattRolle>
)

fun Tiltaksleverandor.toDto(roller: List<AnsattRolle>): VirksomhetDTO {
	if (this.id == null) {
		throw UnsupportedOperationException("Virksomheten er ikke lagret, og kan dermed ikke sendes")
	}

	return VirksomhetDTO(
		id = this.id!!,
		virksomhetsnummer = this.virksomhetsnummer,
		virksomhetsnavn = this.virksomhetsnavn,
		roller = roller
	)
}
