package no.nav.amt.tiltak.bff.nav_ansatt.dto

import java.time.LocalDate
import java.util.*

data class HentGjennomforingMedLopenrDto(
	val id: UUID,
	val navn: String,
	val lopenr: Int,
	val status: Status,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val opprettetAr: Int,
	val arrangorNavn: String,
	val tiltak: TiltakDto,
)
