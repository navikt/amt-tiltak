package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import java.time.LocalDate
import java.util.UUID

data class VeiledersDeltakerDto(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val fodselsnummer: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: DeltakerStatusDto,
	val deltakerliste: DeltakerlisteDto,
	val erMedveilederFor: Boolean
)
