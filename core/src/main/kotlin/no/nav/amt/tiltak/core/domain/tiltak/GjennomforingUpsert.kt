package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.util.*

data class GjennomforingUpsert(
	val id: UUID,
	val tiltakId: UUID,
	val arrangorId: UUID,
	val navn: String,
	val status: Gjennomforing.Status,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val navEnhetId: UUID?,
	val opprettetAar: Int,
	val lopenr: Int,
	val erKurs: Boolean
)

