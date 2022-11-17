package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDateTime

data class DeltakerStatusDto(
	val type: DeltakerStatus.Type,
	val endretDato: LocalDateTime //== createdAt
)

fun DeltakerStatus.toDto() = DeltakerStatusDto(
	type = type,
	endretDato = opprettetDato,
)
