package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import java.time.ZonedDateTime
import java.util.*

data class AnsattRolleDbo(
	val id: UUID,
	val ansattId: UUID,
	val arrangorId: UUID,
	val rolle: ArrangorAnsattRolle,
	val createdAt: ZonedDateTime,
	val gyldigFra: ZonedDateTime,
	val gyldigTil: ZonedDateTime,
)
