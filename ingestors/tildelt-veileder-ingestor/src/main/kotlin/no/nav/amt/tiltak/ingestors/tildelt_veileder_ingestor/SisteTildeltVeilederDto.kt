package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor

import java.time.ZonedDateTime

internal data class SisteTildeltVeilederDto(
	val aktorId: String,
	val veilederId: String,
	val tilordnet: ZonedDateTime,
)
