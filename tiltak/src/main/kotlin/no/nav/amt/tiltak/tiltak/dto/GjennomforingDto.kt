package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.time.LocalDate
import java.util.*

data class GjennomforingDto(
	val id: UUID,
	val navn: String,
	val oppstartdato: LocalDate?,
	val sluttdato: LocalDate?,
	val status: Gjennomforing.Status?,
	val tiltak: TiltakDto
)

fun Gjennomforing.toDto() = GjennomforingDto(
	id = this.id,
	navn = this.navn,
	oppstartdato = this.oppstartDato,
	sluttdato = this.sluttDato,
	status = this.status,
	tiltak = this.tiltak.toDto()
)
