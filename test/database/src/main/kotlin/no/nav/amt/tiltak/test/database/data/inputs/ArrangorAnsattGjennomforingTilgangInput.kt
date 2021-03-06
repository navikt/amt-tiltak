package no.nav.amt.tiltak.test.database.data.inputs

import java.time.ZonedDateTime
import java.util.*

data class ArrangorAnsattGjennomforingTilgangInput(
	val id: UUID,
	val ansattId: UUID,
	val gjennomforingId: UUID,
	val gyldigFra: ZonedDateTime,
	val gyldigTil: ZonedDateTime
)
