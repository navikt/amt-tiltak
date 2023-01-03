package no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor

import java.time.LocalDate
import java.util.*

data class GjennomforingMessage(
	val id: UUID,
	val tiltakstype: Tiltakstype,
	val navn: String?,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
)

data class Tiltakstype(
	val id: UUID,
	val navn: String,
	val arenaKode: String,
)
