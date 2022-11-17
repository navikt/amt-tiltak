package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDateTime
import java.util.*

data class DeltakerStatusInsertDbo(
	val id: UUID,
	val deltakerId: UUID,
	val type: Deltaker.Status,
	val aarsak: DeltakerStatus.Aarsak?,
	val gyldigFra: LocalDateTime?
)
