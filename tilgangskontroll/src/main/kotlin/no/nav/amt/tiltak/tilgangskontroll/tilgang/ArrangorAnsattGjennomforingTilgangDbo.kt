package no.nav.amt.tiltak.tilgangskontroll.tilgang

import java.time.ZonedDateTime
import java.util.*

data class ArrangorAnsattGjennomforingTilgangDbo(
	val id: UUID,
	val ansattId: UUID,
	val gjennomforingId: UUID,
	val gyldigFra: ZonedDateTime,
	val gyldigTil: ZonedDateTime,
	val createdAt: ZonedDateTime,
)
