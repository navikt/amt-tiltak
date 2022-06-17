package no.nav.amt.tiltak.test.database.data.commands

import java.util.*

data class InsertArrangorAnsattGjennomforingTilgang(
	val id: UUID,
	val ansattId: UUID,
	val gjennomforingId: UUID,
)
