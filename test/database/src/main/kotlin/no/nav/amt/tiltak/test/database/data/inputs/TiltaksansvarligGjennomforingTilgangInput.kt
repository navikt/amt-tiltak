package no.nav.amt.tiltak.test.database.data.inputs

import java.time.ZonedDateTime
import java.util.*

data class TiltaksansvarligGjennomforingTilgangInput(
	val id: UUID,
	val navAnsattId: UUID,
	val gjennomforingId: UUID,
	val gyldigTil: ZonedDateTime,
	val createdAt: ZonedDateTime,
)
