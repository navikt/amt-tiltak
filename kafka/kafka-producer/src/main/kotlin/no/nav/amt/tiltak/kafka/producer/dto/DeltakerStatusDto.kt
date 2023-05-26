package no.nav.amt.tiltak.kafka.producer.dto

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDateTime

data class DeltakerStatusDto(
	val type: DeltakerStatus.Type,
	val aarsak: DeltakerStatus.Aarsak?,
	val opprettetDato: LocalDateTime,
)

fun DeltakerStatus.toDto() = DeltakerStatusDto(
	type = type,
	aarsak = aarsak,
	opprettetDato = opprettetDato
)
