package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.time.LocalDate
import java.util.*

data class GjennomforingDto(
	val id: UUID,
	val navn: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: Gjennomforing.Status,
	val tiltak: TiltakDto,
	val arrangor: ArrangorDto,
	val koordinatorer: List<String>
)

fun Gjennomforing.toDto() = GjennomforingDto(
	id = this.id,
	navn = this.navn,
	startDato = this.startDato,
	sluttDato = this.sluttDato,
	status = this.status,
	tiltak = this.tiltak.toDto(),
	arrangor = this.arrangor.toDto(),
	koordinatorer = this.koordinatorer
)
