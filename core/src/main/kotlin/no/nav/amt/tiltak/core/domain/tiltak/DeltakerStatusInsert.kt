package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.UUID

data class DeltakerStatusInsert(
	val id: UUID,
	val deltakerId: UUID,
	val type: DeltakerStatus.Type,
	val aarsak: DeltakerStatus.Aarsak?,
	val aarsaksbeskrivelse: String?,
	val gyldigFra: LocalDateTime?
)
