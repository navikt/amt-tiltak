package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import java.time.LocalDate

data class AktivEndringsmeldingDto(
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
)

fun Endringsmelding.toDto() = AktivEndringsmeldingDto(
	startDato = startDato,
	sluttDato = sluttDato,
)
