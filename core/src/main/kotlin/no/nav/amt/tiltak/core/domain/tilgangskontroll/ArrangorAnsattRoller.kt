package no.nav.amt.tiltak.core.domain.tilgangskontroll

import java.util.*

data class ArrangorAnsattRoller(
	val arrangorId: UUID,
	val roller: List<ArrangorAnsattRolle>
)
