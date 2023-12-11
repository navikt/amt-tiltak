package no.nav.amt.tiltak.external.api.dto

import java.util.UUID

data class GjennomforingDto(
	val id: UUID,
	val navn: String,
	val type: String, //Arena type
	val tiltakstypeNavn: String,
	val arrangor: ArrangorDto
)
