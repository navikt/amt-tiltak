package no.nav.amt.tiltak.tiltak.controllers.dto

import no.nav.amt.tiltak.core.domain.tiltak.Deltager
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class TiltakDeltagerDto(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsdato: LocalDate?,
	val startdato: ZonedDateTime,
	val sluttdato: ZonedDateTime,
	val status: Deltager.Status
)
