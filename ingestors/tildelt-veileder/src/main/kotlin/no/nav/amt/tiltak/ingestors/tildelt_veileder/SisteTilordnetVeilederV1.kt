package no.nav.amt.tiltak.ingestors.tildelt_veileder

import java.time.ZonedDateTime

data class SisteTilordnetVeilederV1(
	val aktorId: String,
	val veilederId: String,
	val tilordnet: ZonedDateTime,
)
