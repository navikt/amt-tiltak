package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import java.util.UUID

data class LeggTilVeiledereRequest(
	val veiledere: List<VeilederRequest>,
)
 data class VeilederRequest(
	val ansattId: UUID,
	val erMedveileder: Boolean,
)
