package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.*

data class DeltakerStatusInsert(
	val id: UUID,
	val deltakerId: UUID,
	val type: DeltakerStatus.Type,
	val aarsak: DeltakerStatus.Aarsak?,
	val gyldigFra: LocalDateTime?
)
