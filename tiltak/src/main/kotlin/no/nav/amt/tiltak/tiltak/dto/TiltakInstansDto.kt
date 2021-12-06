package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import java.time.LocalDate
import java.util.*

data class TiltakInstansDto(
	val id: UUID,
	val navn: String,
	val oppstartdato: LocalDate?,
	val sluttdato: LocalDate?,
	val status: TiltakInstans.Status?,
	val tiltak: TiltakDto
)

fun TiltakInstans.toDto() = TiltakInstansDto(
	id = this.id,
	navn = this.navn,
	oppstartdato = this.oppstartDato,
	sluttdato = this.sluttDato,
	status = this.status,
	tiltak = this.tiltak.toDto()
)
