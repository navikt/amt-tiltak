package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.repository

import java.util.UUID

data class AnsattGjennomforingCount(
	val ansattId: UUID?,
	val gjennomforinger: Int?
)
