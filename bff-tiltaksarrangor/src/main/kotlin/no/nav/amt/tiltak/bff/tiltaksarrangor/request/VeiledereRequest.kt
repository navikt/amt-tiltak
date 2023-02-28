package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import java.util.*

data class LeggTilVeiledereBulkRequest(
	val deltakerIder: List<UUID>,
	val veiledere: List<VeilederRequest>,
	val gjennomforingId: UUID,
)

data class LeggTilVeiledereRequest(
	val veiledere: List<VeilederRequest>,
)
 data class VeilederRequest(
	val ansattId: UUID,
	val erMedveileder: Boolean,
)
