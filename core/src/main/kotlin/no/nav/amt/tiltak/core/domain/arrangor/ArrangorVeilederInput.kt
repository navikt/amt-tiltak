package no.nav.amt.tiltak.core.domain.arrangor

import java.util.*

data class ArrangorVeilederInput(
	val ansattId: UUID,
	val erMedveileder: Boolean,
)
