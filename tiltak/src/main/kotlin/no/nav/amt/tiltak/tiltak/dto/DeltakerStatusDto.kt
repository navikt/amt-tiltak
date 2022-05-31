package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDateTime

data class DeltakerStatusDto(
	val type: Deltaker.Status,
	val gjelderFra: LocalDateTime,
	val endretDato: LocalDateTime? //= createdAt
){
	init {
	    require(endretDato != null) {"DeltakerStatusDto.endretDato kan ikke være null"}
	}
}
