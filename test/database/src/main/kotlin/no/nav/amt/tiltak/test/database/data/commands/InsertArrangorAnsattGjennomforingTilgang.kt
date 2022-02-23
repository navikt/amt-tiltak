package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertArrangorAnsattGjennomforingTilgang(
	val id: UUID,
	val ansatt_id: UUID,
	val gjennomforing_id: UUID,
)
