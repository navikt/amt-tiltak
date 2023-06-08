package no.nav.amt.tiltak.external.api.dto

import java.util.*

data class GjennomforingDto(
	val id: UUID,
	val navn: String,
	val type: String, //Arena type
	val arrangor: ArrangorDto
)
