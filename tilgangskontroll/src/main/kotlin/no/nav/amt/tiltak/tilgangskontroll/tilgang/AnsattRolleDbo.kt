package no.nav.amt.tiltak.tilgangskontroll.tilgang

import java.time.ZonedDateTime
import java.util.*

data class AnsattRolleDbo(
	val id: UUID,
	val ansattId: UUID,
	val arrangorId: UUID,
	val rolle: AnsattRolle,
	val createdAt: ZonedDateTime,
)
