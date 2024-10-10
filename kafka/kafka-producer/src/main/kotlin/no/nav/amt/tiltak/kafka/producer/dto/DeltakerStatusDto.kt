package no.nav.amt.tiltak.kafka.producer.dto

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.getStatustekst
import no.nav.amt.tiltak.core.domain.tiltak.getVisningsnavn
import java.time.LocalDateTime

data class DeltakerStatusDto(
	val type: DeltakerStatus.Type,
	val statusTekst: String,
	val aarsak: DeltakerStatus.Aarsak?,
	val aarsakTekst: String?,
	val opprettetDato: LocalDateTime,
)

fun DeltakerStatus.toDto() = DeltakerStatusDto(
	type = type,
	statusTekst = type.getStatustekst(),
	aarsak = aarsak,
	aarsakTekst = aarsak?.getVisningsnavn(aarsaksbeskrivelse),
	opprettetDato = opprettetDato
)
