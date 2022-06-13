package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDateTime
import java.util.*

data class DeltakerStatusInsertDbo(
	val id: UUID,
	val deltakerId: UUID,
	val type: Deltaker.Status,
	val gyldigFra: LocalDateTime?
)
