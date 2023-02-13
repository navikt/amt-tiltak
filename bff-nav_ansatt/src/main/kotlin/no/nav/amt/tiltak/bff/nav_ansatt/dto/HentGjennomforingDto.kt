package no.nav.amt.tiltak.bff.nav_ansatt.dto

import java.time.LocalDate
import java.util.*

data class HentGjennomforingDto(
	val id: UUID,
	val navn: String,
	val tiltakNavn: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val arrangor: ArrangorDto,
	val lopenr: Int,
	val opprettetAr: Int,
	val tiltak: TiltakDto,
	val status: Status
)
