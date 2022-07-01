package no.nav.amt.tiltak.test.database.data.commands

import java.time.ZonedDateTime
import java.util.*

data class InsertArrangorAnsattGjennomforingTilgang(
	val id: UUID,
	val ansattId: UUID,
	val gjennomforingId: UUID,
	val gyldigFra: ZonedDateTime,
	val gyldigTil: ZonedDateTime
)
