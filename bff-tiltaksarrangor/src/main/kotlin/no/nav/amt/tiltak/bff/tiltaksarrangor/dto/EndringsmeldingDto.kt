package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import java.time.LocalDate
import java.util.*

data class EndringsmeldingDto(
	val id: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val aktiv: Boolean,
)
