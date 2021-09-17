package no.nav.amt.tiltak.tiltak.controllers.dto

import java.time.ZonedDateTime
import java.util.*

data class TiltakInstansDTO(
	val id: UUID,
	val navn: String,
	val startdato: ZonedDateTime,
	val sluttdato: ZonedDateTime,
	val status: String, // TODO: Egentlig en enum, f.eks GJENNOMFORES (kan utledes fra dato?)
	val antallDeltakere: Int,
	val deltakerKapasitet: Int,
)
