package no.nav.amt.tiltak.tilgangskontroll.tilgang

import java.time.ZonedDateTime
import java.util.*

data class GjennomforingTilgangDbo(
	val id: UUID,
	val ansattId: UUID,
	val gjennomforingId: UUID,
	val opprettetAvNavAnsattId: UUID?,
	val stoppetAvNavAnsattId: UUID?,
	val stoppetTidspunkt: ZonedDateTime?,
	val createdAt: ZonedDateTime,
)
