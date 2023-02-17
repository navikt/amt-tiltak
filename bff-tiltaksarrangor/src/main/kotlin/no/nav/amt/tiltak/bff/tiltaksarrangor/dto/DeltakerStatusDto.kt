package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDateTime

data class DeltakerStatusDto(
	val type: StatusTypeDto,
	val endretDato: LocalDateTime //== createdAt
)

enum class StatusTypeDto {
	VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL
}

fun DeltakerStatus.toDto() = DeltakerStatusDto(
	type = StatusTypeDto.valueOf(type.toString()),
	endretDato = opprettetDato,
)
