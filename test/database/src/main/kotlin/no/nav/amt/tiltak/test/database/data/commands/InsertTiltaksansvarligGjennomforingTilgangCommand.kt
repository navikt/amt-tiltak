package no.nav.amt.tiltak.test.database.data.commands

import java.time.ZonedDateTime
import java.util.*

data class InsertTiltaksansvarligGjennomforingTilgangCommand(
	val id: UUID,
	val navAnsattId: UUID,
	val gjennomforingId: UUID,
	val gyldigTil: ZonedDateTime,
	val createdAt: ZonedDateTime,
)
