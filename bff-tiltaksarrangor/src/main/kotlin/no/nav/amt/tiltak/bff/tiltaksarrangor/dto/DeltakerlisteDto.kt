package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.util.UUID

data class DeltakerlisteDto(
	val id: UUID,
	val navn: String,
	val type: String
)

fun Gjennomforing.toDeltakerlisteDto() = DeltakerlisteDto(
	id = id,
	navn = navn,
	type = tiltak.navn
)
