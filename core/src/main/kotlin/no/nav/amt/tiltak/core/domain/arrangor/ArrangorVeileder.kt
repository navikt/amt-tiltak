package no.nav.amt.tiltak.core.domain.arrangor

import java.util.*

data class ArrangorVeileder (
	val id: UUID,
	val ansattId: UUID,
	val deltakerId: UUID,
	val erMedveileder: Boolean,
)
