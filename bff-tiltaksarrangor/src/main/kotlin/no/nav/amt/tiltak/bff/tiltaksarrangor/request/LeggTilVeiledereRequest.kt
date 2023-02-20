package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import java.util.*

data class LeggTilVeiledereRequest(
	val deltakerIder: List<UUID>,
	val veiledere: List<Veileder>,
	val gjennomforingId: UUID,

) {
	data class Veileder(
		val ansattId: UUID,
		val erMedveileder: Boolean,
	)
}

