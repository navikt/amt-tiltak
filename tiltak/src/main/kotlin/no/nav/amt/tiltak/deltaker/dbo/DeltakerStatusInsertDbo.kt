package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDateTime
import java.util.UUID

data class DeltakerStatusInsertDbo(
	val id: UUID,
	val deltakerId: UUID,
	val type: DeltakerStatus.Type,
	val aarsak: DeltakerStatus.Aarsak?,
	val aarsaksbeskrivelse: String?,
	val gyldigFra: LocalDateTime?
)
