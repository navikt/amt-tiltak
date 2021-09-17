package no.nav.amt.tiltak.tiltak.controllers.dto

import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class TiltakDeltakerDto(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsdato: LocalDate?,
	val startdato: ZonedDateTime,
	val sluttdato: ZonedDateTime,
	val status: String
)
