package no.nav.amt.tiltak.core.domain.tilgangskontroll

import java.time.ZonedDateTime
import java.util.*

data class ArrangorAnsattGjennomforingTilgang(
	val id: UUID,
	val ansattId: UUID,
	val gjennomforingId: UUID,
	val createdAt: ZonedDateTime,
)
