package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.time.LocalDate
import java.util.UUID

data class DeltakerlisteDto(
	val id: UUID,
	val navn: String,
	val type: String,
	val startdato: LocalDate?,
	val sluttdato: LocalDate?
)

fun Gjennomforing.toDeltakerlisteDto() = DeltakerlisteDto(
	id = id,
	navn = navn,
	type = tiltak.navn,
	startdato = if (erKurs) startDato else null,
	sluttdato = if (erKurs) sluttDato else null
)
