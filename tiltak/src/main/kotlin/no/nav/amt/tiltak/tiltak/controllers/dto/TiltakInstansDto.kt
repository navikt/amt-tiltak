package no.nav.amt.tiltak.tiltak.controllers.dto

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import java.time.ZonedDateTime
import java.util.*

data class TiltakInstansDto(
	val id: UUID,
	val navn: String,
	val startdato: ZonedDateTime,
	val sluttdato: ZonedDateTime,
	val status: TiltakInstans.Status, // TODO: Avgjøre om man skal DTOifisere denne
	val antallDeltagere: Int,
	val deltagerKapasitet: Int,
	val tiltak: TiltakDto
)
