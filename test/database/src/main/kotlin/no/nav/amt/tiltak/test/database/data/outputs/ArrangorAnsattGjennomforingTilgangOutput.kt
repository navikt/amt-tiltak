package no.nav.amt.tiltak.test.database.data.outputs

import java.time.ZonedDateTime
import java.util.*

data class ArrangorAnsattGjennomforingTilgangOutput(
	val id: UUID,
	val ansattId: UUID,
	val gjennomforingId: UUID,
	val gyldigFra: ZonedDateTime,
	val gyldigTil: ZonedDateTime,
	val createdAt: ZonedDateTime,
)
