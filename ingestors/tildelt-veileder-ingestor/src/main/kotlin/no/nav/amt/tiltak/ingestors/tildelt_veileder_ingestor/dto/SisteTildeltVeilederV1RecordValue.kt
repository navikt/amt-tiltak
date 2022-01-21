package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor.dto

import java.time.ZonedDateTime

data class SisteTildeltVeilederV1RecordValue(
	val aktorId: String,
	val veilederId: String,
	val tilordnet: ZonedDateTime,
)
